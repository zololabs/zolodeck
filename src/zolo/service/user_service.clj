(ns zolo.service.user-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.store.user-store :as u-store]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.setup.config :as conf]
            [zolo.service.core :as service]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]))

(defn- log-into-fb-chat [user]
  (future
    (fb-chat/connect-user! user)
    nil)
  user)

(defn create-new-user [ui]
  {:user/user-identities [ui]})

(defn- update-with-extended-fb-auth-token [user]
  (let [fb-ui (user-identity/fb-user-identity user)
        e-at (fb-gateway/extended-access-token (:identity/auth-token fb-ui) (conf/fb-app-id) (conf/fb-app-secret))]
    (user/update-with-extended-fb-auth-token user e-at)))

(defn extend-fb-token [u]
  (-> u
      update-with-extended-fb-auth-token
      u-store/save))

(defn- find-user [request-params]
  (u-store/find-by-provider-and-provider-uid
   (social/provider request-params)
   (social/provider-uid request-params)))

(def val-request
  {:login_provider [:required :string]
   :login_provider_uid [:required :string]
   :access_token [:required :string]
   :permissions_granted [:required]
   :login_tz [:required :integer]
   :guid [:optional :string]
   :updated [:optional]})

;; Services
;;TODO Need to Check Permissions Granted. Only when Permission is
;;Granted it should proceed to get more info about the user
(defn new-user [request-params]
  (-> request-params
      (service/validate-request! val-request)
      social/fetch-user-identity
      create-new-user
      update-with-extended-fb-auth-token
      (user/update-tz-offset (:login_tz request-params))
      log-into-fb-chat
      u-store/save
      user/distill))

(defn get-users [request-params]
  (-> (find-user request-params)
      user/distill))

(defn get-user-by-guid [guid]
  (-> (u-store/find-by-guid guid)
      user/distill))

;;TODO Need to Check Permissions Granted. Only when Permission is
;;Granted it should proceed to get more info about the user
(defn update-user [guid request-params]
  (-not-nil-> (u-store/find-by-guid guid)
              update-with-extended-fb-auth-token
              (user/update-permissions-granted (:permissions_granted request-params))
              (user/update-tz-offset (:login_tz request-params))
              log-into-fb-chat
              u-store/save
              user/distill))

;;TODO clean up
(defn refresh-user-data [u]
  (let [first-name (user/first-name u)]
    (logger/trace first-name "RefreshUserData... starting now!")
    (let [updated-u (-> u
                        u-store/reload
                        u-store/stamp-refresh-start
                        extend-fb-token
                        c-service/update-contacts-for-user)]
      (logger/info first-name "Loaded contacts " (count (:user/contacts updated-u)))
      
      (let [updated-u (m-service/update-inbox-messages updated-u)]
        (logger/info first-name "Inbox messages done for " (count (:user/contacts updated-u)) " contacts")    
        ;; (message/update-feed-messages-for-all-contacts (u-store/reload u))
        ;; (logger/info first-name "Feed messages done for " (count (:user/contacts (u-store/reload u))) " contacts")
        (logger/info first-name "Refresh data done")
        updated-u))))

;;TODO clean up
(defn refresh-user-scores [u]
  (let [first-name (user/first-name u)]
    (logger/trace first-name "Scoring " (count (:user/contacts u)) " contacts")
    (let [updated-u (c-service/update-scores u)]
      (logger/info first-name "scoring done")  
      (let [updated-u (u-store/stamp-updated-time updated-u)]
        (logger/info first-name "Refresh Score done")
        updated-u))))
