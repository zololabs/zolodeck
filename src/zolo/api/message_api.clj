(ns zolo.api.message-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.message-service :as m-service]))

;;TODO test
;;POST /users/:guid/contacts/:c-guid/message
(defn send-message [user-guid c-guid request-params]
  (if-let [new-t-message (m-service/new-message user-guid c-guid request-params)]
    {:status (STATUS-CODES :ok)
     :body new-t-message}
    (user-not-found)))