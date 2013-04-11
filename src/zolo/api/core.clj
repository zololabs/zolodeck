(ns zolo.api.core
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]))

(defn resource-not-found []
    {:status (:not-found STATUS-CODES)
     :body {:message "No User found"}})
