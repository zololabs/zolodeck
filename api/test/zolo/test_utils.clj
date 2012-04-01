(ns zolo.test-utils
  (:use [clojure.test :only [run-tests deftest is are testing]])
  (:use [zolo.setup.datomic :only [get-db]])
  (:use zolo.utils.debug)
  (:import (java.sql Time Date Timestamp)))

(defmacro is-not [body]
  `(is (not ~body)))

(defn in-datomic-test [test-fn]
  (binding []))

(defn timestamp-for-test [time-in-millis]
  (str (.toString (Date. time-in-millis)) " " (.toString (Time. time-in-millis))))

(defn now-for-test []
  (timestamp-for-test (System/currentTimeMillis)))

(defn is-same-seq? [seq-a seq-b]
  (is (= (sort seq-a) (sort seq-b))))


