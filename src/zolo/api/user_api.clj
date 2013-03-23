(ns zolo.api.user-api
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.user-service :as u-service]))

;;TODO Need to fix this for REST
;; GET /users 
(defn find-users [request-params]
  (if-let [u (u-service/get-user request-params)]
    u
    (throw+ {:type :not-found :message "No User Found"})))

;;POST /users
(defn new-user [request-params]
  (u-service/new-user request-params))

;;PUT /users/guid
(defn update-user [guid request-params]
  (if-let [u (u-service/update-user guid request-params)]
    u
    (throw+ {:type :not-found :message "No User Found"})))