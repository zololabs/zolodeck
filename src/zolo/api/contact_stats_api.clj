(ns zolo.api.contact-stats-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.store.user-store :as u-store]
            [zolo.service.stats-service :as s-service]))

(defn get-contact-stats [guid]
  (if-let [cs (s-service/contact-stats (u-store/find-by-guid guid))]
    {:status (STATUS-CODES :ok)
     :body cs}
    (resource-not-found)))