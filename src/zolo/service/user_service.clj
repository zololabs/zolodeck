(ns zolo.service.user-service
  (:use zolodeck.utils.debug
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.store.user-store :as u-store]
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
        e-at (fb-gateway/extended-access-token (:identity/auth-token fb-ui) (conf/fb-app-id) (conf/fb-app-secret))]
    (user/update-with-extended-fb-auth-token user e-at)))

(defn- find-user [request-params]
  (u-store/find-by-provider-and-provider-uid
   (social/provider request-params)
   (social/provider-uid request-params)))

;; Services
(defn new-user [request-params]
  (-> request-params
      social/signup-user
      update-with-extended-fb-auth-token          
      log-into-fb-chat
      u-store/save
      user/distill))

(defn get-user [request-params]
  (-> (find-user request-params)
      user/distill))