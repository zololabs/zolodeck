(ns zolo.test.assertions
  (:use zolodeck.utils.debug
        [clojure.test :only [is are]]))

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


