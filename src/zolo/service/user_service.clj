(ns zolo.service.user-service
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.store.user-store :as u-store]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.setup.config :as conf]
            [zolo.service.core :as service]))

(defn- log-into-fb-chat [user]
  (future
    (fb-chat/connect-user! user)
    nil)
  user)

(defn- update-with-extended-fb-auth-token [user]
  (let [fb-ui (user-identity/fb-user-identity user)
        e-at (fb-gateway/extended-access-token (:identity/auth-token fb-ui) (conf/fb-app-id) (conf/fb-app-secret))]
    (user/update-with-extended-fb-auth-token user e-at)))

(defn- find-user [request-params]
  (u-store/find-by-provider-and-provider-uid
   (social/provider request-params)
   (social/provider-uid request-params)))

(def val-request
  {:login_provider [:required]
   :login_provider_uid [:required]
   :access_token [:required]
   :permissions_granted [:required]})

;; Services
(defn new-user [request-params]
  (-> request-params
      (service/validate-request! val-request)
      social/signup-user
      update-with-extended-fb-auth-token          
      log-into-fb-chat
      u-store/save
      user/distill))

(defn get-users [request-params]
  (-> (find-user request-params)
      user/distill))

(defn get-user-by-guid [guid]
  (-> (u-store/find-by-guid guid)
      user/distill))

(defn update-user [guid request-params]
  (-not-nil-> (u-store/find-by-guid guid)
              update-with-extended-fb-auth-token
              (user/update-permissions-granted (:permissions_granted request-params))
              log-into-fb-chat
              u-store/save
              user/distill))