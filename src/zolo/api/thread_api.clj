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
    (resource-not-found "Thread")))

(defn load-thread [user-guid ui-guid message-id]
  (if-let [t (print-vals (t-service/load-thread-details user-guid ui-guid message-id))]
    {:status (STATUS-CODES :ok)
     :body t}
    (resource-not-found "Thread")))

(defn update-thread [user-guid ui-guid message-id {done? :done :as params}]
  (if-let [t (t-service/update-thread-details user-guid ui-guid message-id done?)]
    {:status (STATUS-CODES :ok)
     :body t}
    (resource-not-found "Thread")))