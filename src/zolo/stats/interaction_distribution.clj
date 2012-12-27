(ns zolo.stats.interaction-distribution
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

(defn- interaction-distribution-reducer [distribution interaction]
  (update-in distribution (zolo-cal/get-year-month-week (dom/interaction-date interaction)) plus 1))

(defn interaction-distribution [interactions]
  (reduce interaction-distribution-reducer {} interactions))

(defn- collect-weeks [year month weekly-stats]
  (domapcat (fn [[week number]] (list [year month week] number)) weekly-stats))

(defn- collect-months [year monthly-stats]
  (domapcat (fn [[month weekly-stats]] (collect-weeks year month weekly-stats)) monthly-stats))

(defn by-year-month-week [yearly-stats]
  (->>
   (domapcat (fn [[year monthly-stats]] (collect-months year monthly-stats)) yearly-stats)
   (apply hash-map )))

(defn best-week-printer [[[y m w] n]]
  {:best-week-date (zolo-cal/joda-dt-to-nice-string (.withWeekOfWeekyear (time/date-time y m) w))
   :best-week-interaction-count n})

(defn best-week [interactions]
  (->> interactions
       interaction-distribution
       by-year-month-week
       (sort-by val)
       last
       best-week-printer))

(defn weekly-averages [interactions]
  (let [messages (dom/messages-from-interactions interactions)
        sorted-messages (sort-by dom/message-date messages)
        min-date (-> sorted-messages first dom/message-date)
        max-date (-> sorted-messages last dom/message-date)
        weeks-between (zolo-cal/weeks-between min-date max-date)
        weeks-between (if (zero? weeks-between) 1 weeks-between)
        number-of-interactions (count interactions)]
    {:weekly-average (float (/ number-of-interactions weeks-between))}))

(defn distribution-stats [ibc]
  (let [interactions (dom/interactions-from-ibc ibc)]
    (merge (weekly-averages interactions)
           (best-week interactions))))
