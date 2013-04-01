(ns zolo.api.user-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.user-service :as u-service]))

(defn user-url [u]
  (str "/users/" (:user/guid u)))

;;TODO Need to fix this for REST
;; GET /users 
(defn find-users [request-params]
  (if-let [distilled-u (u-service/get-users request-params)]
    {:status (STATUS-CODES :ok)
     :body distilled-u}
    (user-not-found)))

;;POST /users
(defn new-user [request-params]
  (let [new-u (u-service/new-user request-params)]
    {:status (STATUS-CODES :created)
     :headers {"location" (user-url new-u)}
     :body new-u}))

;;PUT /users/guid
(defn update-user [guid request-params]
  (if-let [distilled-u (u-service/update-user guid request-params)]
    {:status (STATUS-CODES :ok)
     :body distilled-u}
    (user-not-found)))

;; GET /users/guid
(defn find-user [guid]
  (if-let [distilled-u (u-service/get-user-by-guid guid)]
    {:status (STATUS-CODES :ok)
     :body distilled-u}
    (user-not-found)))