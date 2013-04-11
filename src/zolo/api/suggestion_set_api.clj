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
    (resource-not-found))
  
  ;; (let [u (user/find-by-guid-string user-id)
  ;;       ibc (interaction/ibc u)]
  ;;   (-> (client-date-inst (:client-date params))
  ;;       (suggestion-set/find-first-by-client-date)
  ;;       (suggestion-set/format ibc)))
  )

;; ;;GET /users/:guid/suggestion_set/:name
;; (defn find-suggestion-set [user-id ss-name]
;;   (let [u (user/find-by-guid-string user-id)
;;         ibc (interaction/ibc u)]
;;     (-> user-id
;;         user/find-by-guid-string
;;         (suggestion-set/suggestion-set ss-name)
;;         (suggestion-set/format ibc)
;;         web/status-404-if-nil)))

;; ;; POST /users/:guid/suggestion_set
;; (defn new-suggestion-set [user-id request-params]
;;   (->> user-id
;;        user/find-by-guid-string
;;        (suggestion-set/new-suggestion-set {:name request-params})))