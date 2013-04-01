(ns zolo.api.core
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        zolo.web.status-codes
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]))

(defn user-not-found []
    {:status (:not-found STATUS-CODES)
     :body {:message "No User found"}})
