(ns zolo.service.suggestion-set-service-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.service.suggestion-set-service :as ss-service]
            [zolo.domain.suggestion-set :as ss]
            [zolo.personas.shy :as shy-persona]
            [zolodeck.utils.calendar :as zolo-cal]))

(deftest test-find-suggestion-set-for-today

  (demonic-testing "User is not present it should return nil"
    (is (nil? (ss-service/find-suggestion-set-for-today nil))))

  (demonic-testing "Suggestion set is not created for today .. it should create and return"
    (let [shy (shy-persona/create)
          ss-set (ss-service/find-suggestion-set-for-today (:user/guid shy))]

      (is (not (nil? ss-set)))

      (is (= (ss/suggestion-set-name (zolo-cal/now-instant)) (:name ss-set)))

      ;;TODO Check Contact Information
      (is (= 2 (count (:contacts ss-set))))
      ))

  (demonic-testing "Suggestion set is not created for today .. it should NOT create and return"))
