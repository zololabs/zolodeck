(ns zolo.test.assertions.core
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [try+]]
        [clojure.test :only [is are]])
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.messages :as fb-messages]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.demonic.core :as demonic]))

(defn same-value? [v1 v2]
  (cond
   (and (coll? v1) (coll? v2)) (= (sort (apply vector v1)) (sort (apply vector v2)))
   :else (= v1 v2)))

(defn assert-map-values [m1 m1-keys m2 m2-keys]
  (is (= (count m1-keys) (count m2-keys)) "No of keys don't match")

  (doall (map #(is (not (nil? (m1 %))) (str % " shouldn't be nil in m1")) m1-keys))
  (doall (map #(is (not (nil? (m2 %))) (str % " shouldn't be nil in m2")) m2-keys))

  (doall (map #(is (same-value? (%1 m1) (%2 m2)) (str %1 " does not match " %2)) m1-keys m2-keys)))


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