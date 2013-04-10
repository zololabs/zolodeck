(ns zolo.domain.interaction
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.maps :as zolo-maps]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.utils.logger :as logger]
            [zolo.domain.message :as message]
            [zolo.domain.user :as user]))

(defn- within-interaction-time? [previous-msg next-msg]
  (let [gap-in-mins (zolo-cal/minutes-between (message/message-date previous-msg) (message/message-date next-msg))]
    (<= gap-in-mins 120)))

(defn- is-part-of? [interaction msg]
  (if (empty? interaction)
    true
    (within-interaction-time? (last interaction) msg)))

(defn- bucket-by-time [interactions msg]
  (if (is-part-of? (last interactions) msg)
    (conj-at-end (conj-at-end msg (last interactions)) (butlast interactions))
    (conj-at-end [msg] interactions)))

(defn- messages->interactions [msgs]
  (reduce bucket-by-time [] msgs))

(defn interaction-date [i]
  (when-not (empty? i)
    (-> i
        first
        (message/message-date (user/tz-offset-minutes)))))

(defn daily-counts [interactions]
  (if (empty? interactions)
    []
    (let [interactions-dates (map #(zolo-cal/start-of-day-inst (interaction-date %)) interactions)
          interactions-freq (frequencies interactions-dates)
          all-dates (-> interactions-dates first zolo-cal/all-dates-through-today)]
      (reduce (fn [ret date]
                (conj ret [(zolo-cal/date-to-simple-string date) (or (interactions-freq date) 0)])) [] all-dates))))

(defn interactions-by-contacts [imbc]
  (zolo-maps/transform-vals-with imbc (fn [c msgs]
                                        (messages->interactions msgs))))

(defn interactions-from-ibc [ibc]
  (->> ibc
       vals
       (apply concat)
       (sort-by interaction-date)
       squeeze))

(defn messages-from-interactions [interactions]
  (-> interactions
      flatten
      squeeze))

;;TODO test
(defn messages-from-ibc [ibc]
  (-> ibc
      interactions-from-ibc
      messages-from-interactions))

;;TODO test
(defn messages-from-ibc-for-contact [c ibc]
  (-> (ibc c)
      messages-from-interactions))

(defn ibc [user]
  (-> user
      message/inbox-messages-by-contacts
      interactions-by-contacts))