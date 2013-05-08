(ns zolo.social.email.initial-sync
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.calendar :as zcal]
            [zolo.social.email.gateway :as gateway]))

;(def HOURLY-DECREMENTS [1 1 1  1 1 1  1 1 1  1 1 1  6 6 6 6   6 6 6 6   6 6 6 6   6 6 6 6   6 6 6 6   6 6 6 6])
(def HOURLY-DECREMENTS (repeat 9 (* 7 24))) ;63 days

(defn decrement-by-hours [dt hours]
  (.minusHours dt hours))

(defn time-stamps []
  (let [now (zcal/now-joda)]
    (reductions decrement-by-hours now HOURLY-DECREMENTS)))

(defn time-stamp-pairs []
  (->> (time-stamps)
       (map zcal/to-seconds)
       (partition 2 1)
       (map reverse)))

(defn get-messages-for-initial-sync [account-id]
  (->> (time-stamp-pairs)
       (mapcat #(apply gateway/get-messages account-id %))))