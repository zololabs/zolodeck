(ns zolo.api.core
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]))

(defn resource-not-found [resource-name]
    {:status (:not-found STATUS-CODES)
     :body {:message (str "No " resource-name  " found")}})
