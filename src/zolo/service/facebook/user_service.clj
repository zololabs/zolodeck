(ns zolo.service.facebook.user-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.domain.user :as user]
            [zolo.domain.user-identity :as user-identity]
            [zolo.store.user-store :as u-store]
            [zolo.social.core :as social]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.service.core :as service]
            [zolo.service.user-service :as u-service]
            [zolo.setup.config :as conf]))

(defn- log-into-fb-chat [user]
  (future
    (fb-chat/connect-user! user)
    nil)
  user)

(defn- update-with-extended-fb-auth-token
  ([user]
     (let [fb-ui (user-identity/fb-user-identity user)]
       (update-with-extended-fb-auth-token user (:identity/auth-token fb-ui))))
  ([user access-token]
     (let [e-at (fb-gateway/extended-access-token access-token (conf/fb-app-id) (conf/fb-app-secret))]
       (user/update-with-extended-fb-auth-token user e-at))))

(defn- extend-fb-token [u]
  (-> u
      update-with-extended-fb-auth-token
      u-store/save))

;; Services
(defmethod u-service/additional-login-processing social/FACEBOOK [new-user request-params]
  (-> new-user
      (update-with-extended-fb-auth-token (:access_token request-params))
      log-into-fb-chat))

(defmethod u-service/pre-refresh-processing :provider/facebook [u]
  (extend-fb-token u))