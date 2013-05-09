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

;; Services
(defmethod u-service/additional-user-identity-processing social/FACEBOOK [new-user request-params]
  (-> new-user
      (u-service/update-with-extended-fb-auth-token (:access_token request-params))
      log-into-fb-chat))