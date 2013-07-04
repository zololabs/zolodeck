(ns zolo.api.suggestion-set-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.logger :as logger]
            [zolo.service.suggestion-set-service :as ss-service]))

;;GET /users/:guid/suggestion_sets
(defn find-suggestion-sets [user-guid params]
  (if-let [ss (ss-service/find-suggestion-set-for-today user-guid)]
    {:status (STATUS-CODES :ok)
     :body ss}
    (resource-not-found "Suggestion Set")))