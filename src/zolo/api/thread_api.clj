(ns zolo.api.thread-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.thread-service :as t-service]
            [zolo.store.user-store :as u-store]))

;;GET /threads/:action
(defn find-threads [user-guid action]
  (if-let [threads (t-service/find-threads (u-store/find-by-guid user-guid) action)]
    {:status (STATUS-CODES :ok)
     :body threads}
    (resource-not-found)))