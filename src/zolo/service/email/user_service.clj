(ns zolo.service.email.user-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.domain.user :as user]
            [zolo.domain.user-identity :as user-identity]
            [zolo.store.user-store :as u-store]
            [zolo.social.core :as social]
            [zolo.service.core :as service]
            [zolo.setup.config :as conf]
            [zolo.service.user-service :as u-service]))

;; Services
(defmethod u-service/additional-login-processing social/EMAIL [new-user request-params]
  new-user)

(defmethod u-service/pre-refresh-processing :provider/email [u]
  u)