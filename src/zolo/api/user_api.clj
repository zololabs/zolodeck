(ns zolo.api.user-api
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.social.core :as social-core]
            [zolo.utils.logger :as logger]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user new?]
  {:guid (str (:user/guid user))
   :email (user-identity/fb-email user)
   :new new?})

(defn log-into-fb-chat [user]
  (future
    (fb-chat/connect-user! user)
    nil)
  user)

(defn find-user [request-params]
  (user/find-by-provider-and-provider-uid
   (social-core/provider request-params)
   (social/provider-uid request-params)))

(defn- update-user-creds [user request-params]
  (user/update-creds user (social/fetch-creds request-params))
  (user/update-permissions-granted user (:permissions_granted request-params))
  (log-into-fb-chat user)
  (format-user user false))


;;TODO Need to fix this for REST
;; GET /users 
(defn find-users [request-params]
  (->
   (find-user request-params)
   (format-user false)))

;;PUT /users/guid
(defn update-user [guid request-params]
  (print-vals "update USER : " guid request-params)
  (if-let [u (user/find-by-guid-string guid)]
    (update-user-creds u request-params)))