(ns zolo.domain.suggestion-set
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.contact :as contact]
            [zolo.domain.accessors :as dom]
            [zolo.domain.interaction :as interaction]
            [zolo.stats.activity :as activity]
            [zolodeck.demonic.core :as demonic]
            [zolo.utils.calendar :as zolo-cal]))

(defn suggestion-set-name [client-date]
  (str "ss-"
       (zolo-cal/year-from-instant client-date) "-"
       (zolo-cal/month-from-instant client-date) "-"
       (zolo-cal/date-from-instant client-date)))

;; (defn suggestion-set-contacts [u]
;;   (let [imbc (dom/inbox-messages-by-contacts u)
;;         ibc (interaction/interactions-by-contacts imbc)]
;;     (activity/forgetting-contacts ibc 5)))

;; (defn new-suggestion-set [u ss-name]
;;   (->> {:suggestion-set/name ss-name
;;         :suggestion-set/contacts (suggestion-set-contacts u)}
;;        (conj (:user/suggestion-sets u))
;;        (assoc u :user/suggestion-sets)
;;        demonic/insert))

;; (defn suggestion-set [u ss-name]
;;   (->> u
;;        print-vals
;;        :user/suggestion-sets
;;        print-vals
;;        (filter #(= ss-name (:suggestion-set/name %)))
;;        first))

;; (defn find-first-by-client-date [client-date]
;;   (->
;;    (demonic/run-query '[:find ?e :in $ ?cdi :where [?e :suggestion-set/client-date ?cdi]] client-date)
;;    ffirst
;;    demonic/load-entity))

;; (defn format [ss ibc]
;;   {:name (:suggestion-set/name ss)
;;    :contacts (domap #(contact/format % ibc (:suggestion-set/client-date ss)) (:suggestion-set/contacts ss))})