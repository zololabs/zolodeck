(ns zolo.domain.suggestion-set
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.contact :as contact]
            [zolo.demonic.core :as demonic]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.domain.suggestion-set.strategy.random :as ss-s-random]))

(defn suggestion-set-name [client-date]
  (str "ss-"
       (zolo-cal/year-from-instant client-date) "-"
       (zolo-cal/month-from-instant client-date) "-"
       (zolo-cal/date-from-instant client-date)))

(defn- reason-for-suggesting [c ibc]
  (let [days-not-contacted (contact/days-not-contacted c ibc)]
    (if (= -1 days-not-contacted)
      "You haven't connected in a while"
      (str "Your last interaction was " days-not-contacted " days ago"))))

(defn- contact-info [c ibc]
  (-> c
      (contact/distill ibc)
      (assoc :contact/reason-to-connect (reason-for-suggesting c ibc))))

(defn new-suggestion-set [u ss-name strategy-fn]
  {:suggestion-set/name ss-name
   :suggestion-set/contacts (strategy-fn u)})

(defn suggestion-set [u ss-name]
  (->> u
       :user/suggestion-sets
       (filter #(= ss-name (:suggestion-set/name %)))
       first))

(defn distill [ss ibc]
  (when ss
    {:suggestion-set/name (:suggestion-set/name ss)
     :suggestion-set/contacts (if (nil? ibc)
                                []
                                (domap #(contact-info % ibc) (:suggestion-set/contacts ss)))}))
