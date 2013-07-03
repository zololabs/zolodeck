(ns zolo.domain.score-test
  (:use zolo.demonic.test
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.personas.generator :as pgen]
            [zolo.domain.score :as score]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.contact :as contact]))

(deftest test-calculate
  (testing "when no messages are send it should be 0"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)]}})
          ibc (interaction/ibc u (:user/contacts u))]
      
      (let [[jack] (sort-by contact/first-name (:user/contacts u))]
        (is (= 0 (score/calculate ibc jack))))))

  (testing "when messages are send score should be send using * 10"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 3 10)
                                                        (pgen/create-friend-spec "Jill" "Ferry" 4 10)
                                                        (pgen/create-friend-spec "Dont" "Care" 0 0)]}})
          ibc (interaction/ibc u (:user/contacts u))]
      
      (let [[dontcare jack jill] (sort-by contact/first-name (:user/contacts u))]
        (is (= 0 (score/calculate ibc dontcare)))
        (is (= 30 (score/calculate ibc jack)))
        (is (= 40 (score/calculate ibc jill)))))))


