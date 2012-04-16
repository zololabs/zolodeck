(ns zolo.test.core-utils
  (:use [clojure.test :only [run-tests deftest is are testing]])
  (:use zolo.utils.debug)
  (:import (java.sql Time Date Timestamp)))

(defmacro is-not [body]
  `(is (not ~body)))

(defn timestamp-for-test [time-in-millis]
  (str (.toString (Date. time-in-millis)) " " (.toString (Time. time-in-millis))))

(defn now-for-test []
  (timestamp-for-test (System/currentTimeMillis)))

(defn is-same-seq? [seq-a seq-b]
  (is (= (sort seq-a) (sort seq-b))))

(defn has-datomic-id? [entity]
  (not (nil? (:db/id entity))))

(defn assert-datomic-id-present [entity]
  (is (has-datomic-id? entity)))

(defn assert-datomic-id-not-present [entity]
  (is (not (has-datomic-id? entity))))

(defn signed-request-for [fb-user-map]
  {:user_id (:id fb-user-map)})

