(ns zolo.api.message-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.utils.http-status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.message-service :as m-service]
            [zolo.store.user-store :as u-store]
            [zolo.store.contact-store :as c-store]))

;;POST /users/:guid/messages
(defn send-message [user-guid params]
  (if-let [new-t-message (m-service/new-message (u-store/find-by-guid user-guid) params)]
    {:status (STATUS-CODES :created) :body new-t-message}
    (resource-not-found "Message")))
