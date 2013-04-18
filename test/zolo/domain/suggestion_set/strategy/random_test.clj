(ns zolo.domain.suggestion-set.strategy.random-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.core :as d-core]
            [zolo.personas.factory :as personas]
            [zolo.personas.generator :as pgen]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.domain.suggestion-set.strategy.random :as ss-s-random]))

(deftest test-compute
  (d-core/run-in-gmt-tz
   (testing "When nil is passed it should return empty"
     (is (empty? (ss-s-random/compute nil))))

   (testing "When user does not have any contact it should return empty"
     (let [u (pgen/generate-domain {:SPECS {:friends []}})]
       (is (empty? (ss-s-random/compute u)))))

   (testing "When user has less than 5 contacts it should return all of them"
     (let [u (pgen/generate-domain {:SPECS {:friends (pgen/create-friend-specs 3)}})]
       (is (= 3 (count (ss-s-random/compute u))))))

   (testing "When user has more than 5 contacts it should return only 5 of them"
     (let [u (pgen/generate-domain {:SPECS {:friends (pgen/create-friend-specs 12)}})]
       (is (= 5 (count (ss-s-random/compute u))))))

   (testing "When user has contacts that are contacted today should not be part of suggestion set"
     (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                                      (pgen/create-friend-spec "Jill" "Ferry" 3 6)]}})]
       
       (let [[jack jill] (sort-by contact/first-name (:user/contacts u))]
         (testing "When not contacted"
           (run-as-of "2012-05-09"
             (is (= 2 (count (ss-s-random/compute u))))))
         
         (testing "When contacted"
           (run-as-of "2012-05-12"
             (is (= 1 (count (ss-s-random/compute u))))
             (is (= (:contact/guid jack) (-> (ss-s-random/compute u) first :contact/guid))))))))


   (testing "Muted Contacts should not be part of Suggestion Set"
     (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels")
                                                      (pgen/create-friend-spec "Jill" "Ferry")]}})]
       
       (let [[jack jill] (sort-by contact/first-name (:user/contacts u))
             muted-jack (merge jack {:contact/muted true})
             updated-u (assoc u :user/contacts [muted-jack jill])]
         
         (is (= 1 (count (ss-s-random/compute updated-u))))
         (is (= (:contact/guid jill) (-> (ss-s-random/compute updated-u) first :contact/guid)))))))) 