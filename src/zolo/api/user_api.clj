(ns zolo.api.user-api
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.user-service :as user-service]))

(defn- update-user-creds [user request-params]
  ;; (user/update-creds user (social/fetch-creds request-params))
  ;; (user/update-permissions-granted user (:permissions_granted request-params))
  ;; (log-into-fb-chat user)
  ;; (format-user user)
  )

;;TODO Need to fix this for REST
;; GET /users 
(defn find-users [request-params]
  (if-let [u (user-service/get-user request-params)]
    u
    (throw+ {:type :not-found :message "No User Found"})))

;;POST /users
(defn new-user [request-params]
  (user-service/new-user request-params))

;; ;;PUT /users/guid
;; (defn update-user [guid request-params]
;;   (if-let [u (user/find-by-guid-string guid)]
;;     (update-user-creds u request-params)
;;     (throw+ {:type :not-found :message "No User Found"})))