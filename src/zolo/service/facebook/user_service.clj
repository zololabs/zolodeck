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
(defmethod u-service/new-user social/FACEBOOK [request-params]
  (-> request-params
      (service/validate-request! val-request)
      social/fetch-user-identity
      u-service/create-new-user
      (u-service/update-with-extended-fb-auth-token (:access_token request-params))
      (user/update-tz-offset (:login_tz request-params))
      log-into-fb-chat
      u-store/save
      user/distill))


;;TODO Need to Check Permissions Granted. Only when Permission is
;;Granted it should proceed to get more info about the user
(defmethod u-service/update-user social/FACEBOOK [guid request-params]
  (-not-nil-> (u-store/find-by-guid guid)
              (u-service/update-with-extended-fb-auth-token (:access_token request-params))
              (user/update-permissions-granted (:permissions_granted request-params))
              (user/update-tz-offset (:login_tz request-params))
              log-into-fb-chat
              u-store/save
              user/distill))