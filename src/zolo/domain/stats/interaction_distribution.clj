(ns zolo.domain.stats.interaction-distribution
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.interaction :as interaction]
            [zolo.domain.message :as message]
            [zolo.utils.calendar :as zcal]
            [clj-time.core :as time]
            [clj-time.coerce :as ctc]))

;;TODO test this Whole namespace

;; (defn all-distribution-keys [min-year max-year]
;;   (for [year (range min-year (inc max-year)) month (range 1 13) week (range 1 53)]
;;            [year month week]))

(defn plus [x y]
  (if-not x y (+ x y)))

(defn- interaction-distribution-reducer [distribution interaction]
  (update-in distribution (zcal/get-year-month-week (interaction/interaction-date interaction)) plus 1))

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
  {:best-week-date (zcal/joda-dt-to-nice-string (.withWeekOfWeekyear (time/date-time y m) w))
   :best-week-interaction-count n})

(defn best-week [interactions]
  (->> interactions
       interaction-distribution
       by-year-month-week
       (sort-by val)
       last
       best-week-printer))

(defn best-week-interaction-count [interactions]
  (:best-week-interaction-count (best-week interactions)))

(defn weekly-average [interactions]
  (let [messages (interaction/messages-from-interactions interactions)
        sorted-messages (sort-by message/message-date messages)
        min-date (-> sorted-messages first message/message-date)
        max-date (-> sorted-messages last message/message-date)
        weeks-between (zcal/weeks-between min-date max-date)
        weeks-between (if (zero? weeks-between) 1 weeks-between)
        number-of-interactions (count interactions)]
    (float (/ number-of-interactions weeks-between))))

;; (defn distribution-stats [ibc]
;;   (let [interactions (dom/interactions-from-ibc ibc)]
;;     (merge (weekly-averages interactions)
;;            (best-week interactions))))
