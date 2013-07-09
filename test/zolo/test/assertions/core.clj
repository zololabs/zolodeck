(ns zolo.test.assertions.core
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [try+]]
        [clojure.test :only [is are]])
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.messages :as fb-messages]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.demonic.core :as demonic]
            [zolo.demonic.loadable :as loadable]))

(defn same-value? [v1 v2]
  (cond
   (and (coll? v1) (coll? v2)) (= (sort (apply vector v1)) (sort (apply vector v2)))
   :else (= v1 v2)))

(defn has-keys [m key-seq]
  (doseq [k key-seq]
    (is (some #{k} (keys m)) (str "Value for key: " k " missing in map: " m))))

(defn assert-map-values [m1 m1-keys m2 m2-keys]
  (let [m1 (loadable/entity->loadable m1)
        m2 (loadable/entity->loadable m2)]
    (is (= (count m1-keys) (count m2-keys)) "No of keys don't match")

    (doall (map #(is (not (nil? (m1 %))) (str % " shouldn't be nil in m1")) m1-keys))
    (doall (map #(is (not (nil? (m2 %))) (str % " shouldn't be nil in m2")) m2-keys))

    (doall (map #(is (same-value? (%1 m1) (%2 m2)) (str %1 " does not match " %2)) m1-keys m2-keys))))

(defn is-same-day? [yyyy-MM-dd-str dt]
  (= yyyy-MM-dd-str
      (str (.getYear dt) "-"
           (.getMonthOfYear dt) "-"
           (.getDayOfMonth dt))))

(defn assert-same-day? [yyyy-MM-dd-str dt]
  (is (is-same-day? yyyy-MM-dd-str dt)))

(defn is-thrown+ [thunk form]
  (let [ex-info-data (try
                      (thunk)
                      nil
                      (catch clojure.lang.ExceptionInfo e
                        (:object (.getData e))))]
    (is (not (nil? ex-info-data)) (str "No exception thrown when running:" form))
    ex-info-data))

(defn check-throw+-info [expected-info thunk form]
  (let [actual (is-thrown+ thunk form)]    
    (is (= expected-info actual))))

(defmacro thrown+? [expected-info form]
  `(check-throw+-info ~expected-info
                      (fn [] ~form)
                      '~form))