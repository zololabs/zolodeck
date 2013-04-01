(ns zolo.domain.suggestion-set
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolo.domain.interaction :as interaction]
            [zolo.stats.activity :as activity]
            [zolo.demonic.core :as demonic]
            [zolo.utils.calendar :as zolo-cal]))

(defn- suggestion-set-contacts [u]
  ;; (let [imbc (dom/inbox-messages-by-contacts u)
  ;;       ibc (interaction/interactions-by-contacts imbc)]
  ;;   (activity/forgetting-contacts ibc 5))
  (:user/contacts u))

(defn suggestion-set-name [client-date]
  (str "ss-"
       (zolo-cal/year-from-instant client-date) "-"
       (zolo-cal/month-from-instant client-date) "-"
       (zolo-cal/date-from-instant client-date)))

;;TODO Test this
(defn new-suggestion-set [u ss-name]
  {:suggestion-set/name ss-name
   :suggestion-set/contacts (suggestion-set-contacts u)})

(defn suggestion-set [u ss-name]
  (->> u
       :user/suggestion-sets
       (filter #(= ss-name (:suggestion-set/name %)))
       first))

(defn distill [ss]
  {:suggestion-set/name (:suggestion-set/name ss)
   :suggestion-set/contacts (domap contact/distill (:suggestion-set/contacts ss))})
