(ns zolo.domain.suggestion-set
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.contact :as contact]
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

(defn- reason-for-suggesting [c ibc]
  (let [days-not-contacted (contact/days-not-contacted c ibc)]
    (if (= -1 days-not-contacted)
      "You never interacted"
      (str "Your last interaction was " days-not-contacted "  days ago"))))

(defn- contact-info [c ibc]
  (-> c
      contact/distill
      (assoc :contact/reason-to-connect (reason-for-suggesting c ibc))))

;;TODO Test this
(defn new-suggestion-set [u ss-name]
  {:suggestion-set/name ss-name
   :suggestion-set/contacts (suggestion-set-contacts u)})

(defn suggestion-set [u ss-name]
  (->> u
       :user/suggestion-sets
       (filter #(= ss-name (:suggestion-set/name %)))
       first))

;;TODO test
(defn distill [ss ibc]
  {:suggestion-set/name (:suggestion-set/name ss)
   :suggestion-set/contacts (domap #(contact-info % ibc) (:suggestion-set/contacts ss))})
