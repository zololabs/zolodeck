(ns zolo.test.assertions
  (:use zolodeck.utils.debug
        [clojure.test :only [is are]])
  (:require [zolo.domain.zolo-graph :as zg]
            [zolo.domain.zolo-graph.validation :as zg-validation]))

(defn assert-zg-is-valid [zg]
  (is (zg-validation/valid? zg) "Zolo Graph is not VALID"))

(defn assert-zg-is-not-valid [zg]
  (is (not (zg-validation/valid? zg)) "Zolo Graph should be INVALID "))

(defn assert-zg-has-contacts [zg no-of-contacts]
  (is (= no-of-contacts (count (zg/contacts zg)))))

(defn assert-zg-has-no-contacts [zg]
  (assert-zg-has-contacts zg 0))

(defn assert-contacts-are-same [expected-contact actual-contact]
  (is (= (set (keys expected-contact)) (set (keys actual-contact))))
  (map #(is (= (% expected-contact) (% actual-contact))) (keys expected-contact)))