(ns zolo.api.user-api
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.social.core :as social-core]
            [zolo.utils.logger :as logger]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user]
  (if user
    {:guid (str (:user/guid user))
     :email (user-identity/fb-email user)}
    (throw+ {:type :not-found :message "No User Found"})))

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
  (format-user user))


;;TODO Need to fix this for REST
;; GET /users 
(defn find-users [request-params]
  (->
   (find-user request-params)
   format-user))

;;POST /users
(defn insert-user [request-params]
  (-> request-params
      social/signup-user
      user/signup-new-user
      user/update-with-extended-fb-auth-token          
      log-into-fb-chat
      format-user))

;;PUT /users/guid
(defn update-user [guid request-params]
  (if-let [u (user/find-by-guid-string guid)]
    (update-user-creds u request-params)
    (throw+ {:type :not-found :message "No User Found"})))

;;Suggestion Set
(defn find-suggestion-set [guid ss-id]
  (->> guid
      user/find-by-guid-string
      (user/suggestion-set ss-id)))