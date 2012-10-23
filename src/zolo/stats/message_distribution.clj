(ns zolo.stats.message-distribution
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolodeck.utils.calendar :as zolo-cal]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn all-distribution-keys [min-year max-year]
  (for [year (range min-year (inc max-year)) month (range 1 13) week (range 1 53)]
           [year month week]))

(defn plus [x y]
  (if-not x y (+ x y)))

(defn- message-distribution-reducer [distribution message]
  (update-in distribution (zolo-cal/get-year-month-week (:message/date message)) plus 1))

(defn message-distribution [u]
  (->> u :user/contacts (mapcat :contact/messages) (reduce message-distribution-reducer {})))

(defn- collect-weeks [year month weekly-stats]
  (mapcat (fn [[week number]] (list [year month week] number)) weekly-stats))

(defn- collect-months [year monthly-stats]
  (mapcat (fn [[month weekly-stats]] (collect-weeks year month weekly-stats)) monthly-stats))

(defn by-year-month-week [yearly-stats]
  (->>
   (mapcat (fn [[year monthly-stats]] (collect-months year monthly-stats)) yearly-stats)
   (apply hash-map )))

(defn best-week-printer [[[y m w] n]]
  {:best-week-date (zolo-cal/joda-dt-to-nice-string (.withWeekOfWeekyear (time/date-time y m) w))
   :best-week-interaction-count n})

(defn best-week [u]
  (->> u
       message-distribution
       by-year-month-week
       (sort-by val)
       last
       best-week-printer))

(defn first-or-last-message-time-for-user [u sorter]
  (->> u
       :user/contacts
       (mapcat :contact/messages)
       (sorter :message/date)
       last
       :message/date))

(defn first-message-time-for-user [u]
  (first-or-last-message-time-for-user u reverse-sort-by))

(defn last-message-time-for-user [u]
  (first-or-last-message-time-for-user u sort-by))

(defn weekly-averages [u]
  (let [min-date (print-vals "min:" (first-message-time-for-user u))
        max-date (print-vals "max:" (last-message-time-for-user u))
        weeks-between (print-vals "between weeks:" (zolo-cal/weeks-between min-date max-date))
        number-of-messages (print-vals "number-of-msgs:" (count (mapcat :contact/messages (:user/contacts u))))]
    {:weekly-average (float (/ number-of-messages weeks-between))}))

(defn distribution-stats [u]
  (merge (weekly-averages u)
         (best-week u)))