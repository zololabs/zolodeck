(ns zolo.api.suggestion-set-api
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.suggestion-set :as suggestion-set]
            [zolo.domain.accessors :as dom]
            [zolo.web :as web]
            [zolo.utils.logger :as logger]
            [clj-time.format :as ctf]
            [clj-time.core :as time]))

;; (defn- client-date-inst [client-date-rfc]
;;   (let [fmt (:rfc822 clj-time.format/formatters)
;;         lt (ctf/parse-local fmt client-date-rfc)]
;;     (.toDate (time/date-time (time/year lt) (time/month lt) (time/day lt)))))

;; ;;GET /users/:guid/suggestion_set
;; (defn find-suggestion-sets [user-id params]
;;   (let [u (user/find-by-guid-string user-id)
;;         ibc (interaction/ibc u)]
;;     (-> (client-date-inst (:client-date params))
;;         (suggestion-set/find-first-by-client-date)
;;         (suggestion-set/format ibc))))

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