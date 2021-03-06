(ns zolo.api.stats-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.utils.http-status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]]
        metrics.timers)
  (:require [zolo.utils.logger :as logger]
            [zolo.store.user-store :as u-store]
            [zolo.service.stats-service :as s-service]))

(deftimer contacts-stats-time)
(deftimer interaction-stats-time)

(defn get-contact-stats [guid]
  (time! contacts-stats-time
   (if-let [cs (s-service/contact-stats guid)]
     {:status (STATUS-CODES :ok)
      :body cs}
     (resource-not-found "Contact"))))

(defn get-interaction-stats [guid]
  (time! interaction-stats-time
    (if-let [is (s-service/interaction-stats guid)]
      {:status (STATUS-CODES :ok)
       :body is}
      (resource-not-found "Interaction"))))