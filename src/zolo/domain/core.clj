(ns zolo.domain.core
  (:use zolo.utils.debug)
  (:require [zolo.domain.user :as user]))

(defmacro run-in-tz-offset [tz-offset-minutes & body]
  `(binding [user/*tz-offset-minutes* ~tz-offset-minutes]
     ~@body))

(defmacro run-in-gmt-tz [& body]
  `(binding [user/*tz-offset-minutes* 0]
     ~@body))