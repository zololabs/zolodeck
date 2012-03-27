(ns zolo.utils.calendar
  (:use [clj-time.format :only (parse formatters)]))

(defn java-date-from-string [yyyy-MM-dd-string]
  (.toDate (parse (formatters :date) "2012-03-20")))

