(ns zolo.stats.message-distribution
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.domain.accessors :as dom]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.utils.maps :as zolo-maps]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn all-distribution-keys [min-year max-year]
  (for [year (range min-year (inc max-year)) month (range 1 13) week (range 1 53)]
           [year month week]))

(defn plus [x y]
  (if-not x y (+ x y)))

(defn- message-distribution-reducer [distribution message]
  (update-in distribution (zolo-cal/get-year-month-week (:message/date message)) plus 1))

(defn message-distribution [messages]
  (reduce message-distribution-reducer {} messages))

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

(defn best-week [messages]
  (->> messages
       message-distribution
       by-year-month-week
       (sort-by val)
       last
       best-week-printer))

(defn weekly-averages [messages]
  (let [min-date (-> messages first :message/date)
        max-date (-> messages last :message/date)
        weeks-between (zolo-cal/weeks-between min-date max-date)
        number-of-messages (count messages)]
    {:weekly-average (float (/ number-of-messages weeks-between))}))

(defn distribution-stats [imbc]
  (let [messages (->> imbc vals (apply concat))]
    (merge (weekly-averages messages)
           (best-week messages))))