(ns zolo.api.message-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.message-service :as m-service]
            [zolo.store.user-store :as u-store]
            [zolo.store.contact-store :as c-store]))

;;POST /users/:guid/contacts/:c-guid/message
(defn send-message [user-guid c-guid request-params]
  (if-let [new-t-message (m-service/new-message (u-store/find-by-guid user-guid)
                                                (c-store/find-by-guid c-guid)
                                                request-params)]
    {:status (STATUS-CODES :created)}
    (resource-not-found)))