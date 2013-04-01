(ns zolo.test.assertions
  (:use zolo.utils.debug
        [clojure.test :only [is are]])
  (:require [zolo.domain.zolo-graph :as zg]
            [zolo.domain.zolo-graph.validation :as zg-validation]))

;; Zolo Graph related Ones
(defn assert-zg-is-valid [zg]
  (is (zg-validation/valid? zg) "Zolo Graph is not VALID"))

(defn assert-zg-is-not-valid [zg]
  (is (not (zg-validation/valid? zg)) "Zolo Graph should be INVALID "))

(defn assert-zg-has-contacts [zg no-of-contacts]
  (is (= no-of-contacts (count (zg/contacts zg)))))

(defn assert-zg-has-no-contacts [zg]
  (assert-zg-has-contacts zg 0))

(defn assert-zg-contact-has-messages [zg contact no-of-messages]
  (is (= no-of-messages (count (zg/messages zg (:contact/guid contact))))))

(defn assert-zg-contact-has-no-messages [zg contact]
  (assert-zg-contact-has-messages zg contact 0))

(defn assert-zg-contact-has-scores [zg contact no-of-scores]
  (is (= no-of-scores (count (zg/scores zg (:contact/guid contact))))))

(defn assert-zg-contact-has-no-scores [zg contact]
  (assert-zg-contact-has-scores zg contact 0))

;; Domain Related Ones
(defn assert-contacts-are-same [expected-contact actual-contact]
  (is (= (set (keys expected-contact)) (set (keys actual-contact))))
  (map #(is (= (% expected-contact) (% actual-contact))) (keys expected-contact)))

;; Datomic related Ones
(defn has-datomic-id? [entity]
  (not (nil? (:db/id entity))))

(defn assert-datomic-id-present [entity]
  (is (has-datomic-id? entity)))

(defn assert-datomic-id-not-present [entity]
  (is (not (has-datomic-id? entity))))


