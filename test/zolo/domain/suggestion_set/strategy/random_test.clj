(ns zolo.domain.suggestion-set.strategy.random-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.personas.factory :as personas]
            [zolo.personas.generator :as pgen]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.domain.suggestion-set.strategy.random :as ss-s-random]))

(deftest test-compute
  (testing "When nil is passed it should return empty"
    (is (empty? (ss-s-random/compute nil))))

  (testing "When user does not have any contact it should return empty"
    (let [u (pgen/generate-domain {:FACEBOOK {:friends []}})]
      (is (empty? (ss-s-random/compute u)))))

  (testing "When user has less than 5 contacts it should return all of them"
    (let [u (pgen/generate-domain {:FACEBOOK {:friends (pgen/create-friend-specs 3)}})]
      (is (= 3 (count (ss-s-random/compute u))))))

  (testing "When user has more than 5 contacts it should return only 5 of them"
    (let [u (pgen/generate-domain {:FACEBOOK {:friends (pgen/create-friend-specs 12)}})]
      (is (= 5 (count (ss-s-random/compute u)))))))