(ns zolo.api.user-api
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolo.service.user-service :as user-service]))

;;TODO (siva) this is an experiment ..need to change this though

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
(defn new-user [request-params]
  (user-service/new-user request-params))

;;PUT /users/guid
(defn update-user [guid request-params]
  (if-let [u (user/find-by-guid-string guid)]
    (update-user-creds u request-params)
    (throw+ {:type :not-found :message "No User Found"})))