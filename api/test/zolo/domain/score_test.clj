(ns zolo.domain.score-test
  (:use zolodeck.demonic.test
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.score :as score]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.loner :as loner]
            [zolo.personas.shy :as shy]
            [zolo.personas.core :as personas]
            [zolo.test.assertions :as assertions]))

(deftest test-calculate
  (testing "should return 0 when contact is nil"
    (is (= 0 (score/calculate nil))))
  
  (demonic-testing "when no messages are present"
    (let [shy (shy/create)
          jack (personas/friend-of shy "jack")]
      (is (= 0 (score/calculate jack)))))

  (demonic-testing "when there messages  present"
    (let [vincent (vincent/create)
          jack (personas/friend-of vincent "jack")
          jill (personas/friend-of vincent "jill")]
      (is (= 30 (score/calculate jack)))
      (is (= 20 (score/calculate jill))))))