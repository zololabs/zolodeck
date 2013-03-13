(ns zolo.service.user-service
  (:use zolodeck.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.setup.config :as conf]))

(defn- log-into-fb-chat [user]
  (future
    (fb-chat/connect-user! user)
    nil)
  user)

(defn- update-with-extended-fb-auth-token [user]
  (let [fb-ui (user-identity/fb-user-identity user)
        old-at (:identity/auth-token fb-ui)
        extended (fb-gateway/extended-access-token old-at (conf/fb-app-id) (conf/fb-app-secret))]
    (user-identity/update fb-ui {:identity/auth-token extended})
    (user/reload user)))

(defn new-user [request-params]
  (-> request-params
      social/signup-user
      user/create
      update-with-extended-fb-auth-token          
      log-into-fb-chat
      user/distill))