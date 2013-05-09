(ns zolo.api.thread-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.thread-service :as t-service]))

;;GET /threads/:action
(defn find-threads [user-guid {:keys [action] :as params}]
  (if-let [threads (t-service/find-threads user-guid action)]
    {:status (STATUS-CODES :ok)
     :body threads}
    (resource-not-found)))

(defn load-thread-details [user-guid thread-guid provider-uid]
  (if-let [t (t-service/load-gmail-thread-details user-guid provider-uid thread-guid)]
    {:status (STATUS-CODES :ok)
     :body t}
    (resource-not-found)))