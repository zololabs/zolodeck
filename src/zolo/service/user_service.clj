(ns zolo.service.user-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.service.distiller.user :as u-distiller]
            [zolo.domain.user :as user]
            [zolo.domain.message :as m]
            [zolo.store.user-store :as u-store]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.setup.config :as conf]
            [zolo.service.core :as service]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.utils.calendar :as zcal]))

(defn- approx-two-hours-from-now []
  (-> (zcal/now-joda)
      (zcal/plus (+ 90 (rand-int 60)) :minutes)
      zcal/to-inst))

(defn create-new-user [ui]
  {:user/user-identities [ui]
   :user/data-ready-in (approx-two-hours-from-now)})

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
   :updated [:optional]
   :data_ready_in [:optional]
   :all_permissions_granted [:optional]
   :emails [:optional]})

;; Services
(defmulti additional-login-processing (fn [u params] (social/login-dispatcher params)))
(defmulti pre-refresh-processing (fn [u] (-> u :user/user-identities first :identity/provider)))

(defn new-user [request-params]
  (-> request-params
      (service/validate-request! val-request)
      social/fetch-user-identity
      create-new-user
      (user/update-tz-offset (:login_tz request-params))
      (additional-login-processing request-params)
      u-store/save
      u-distiller/distill))

(defn update-user [guid request-params]
  (if-let [u (u-store/find-by-guid guid)]
    (do
      (service/validate-request! request-params val-request)
      (-> u
          (user/update-permissions-granted (:permissions_granted request-params))
          (user/update-tz-offset (:login_tz request-params))
          (additional-login-processing request-params)
          u-store/save
          u-distiller/distill))))

(defn get-users [request-params]
  (-> (find-user request-params)
      u-distiller/distill))

(defn get-user-by-guid [guid]
  (-> (u-store/find-by-guid guid)
      u-distiller/distill))


;;TODO clean up
(defn refresh-user-data [u]
  (let [first-name (user/first-name u)]
    (logger/trace first-name "RefreshUserData... starting now!")
    (let [updated-u (-> u
                        u-store/reload
                        u-store/stamp-refresh-start
                        pre-refresh-processing
                        c-service/update-contacts-for-user)]
      (logger/info first-name "Loaded contacts " (count (:user/contacts updated-u)))
      
      (let [updated-u (m-service/update-inbox-messages updated-u)]
        (logger/info first-name (count (m/all-messages updated-u)) "- Inbox messages done for " (count (:user/contacts updated-u)) " contacts")    
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
