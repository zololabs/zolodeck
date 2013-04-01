(ns zolo.service.suggestion-set-service-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.service.suggestion-set-service :as ss-service]
            [zolo.domain.suggestion-set :as ss]
            [zolo.domain.contact :as contact]
            [zolo.personas.shy :as shy-persona]
            [zolo.utils.calendar :as zolo-cal]))

(deftest test-find-suggestion-set-for-today

  (demonic-testing "User is not present it should return nil"
    (is (nil? (ss-service/find-suggestion-set-for-today nil))))

  (demonic-testing "Suggestion set is not created for today .. it should create and return"
    (stubbing [zolo-cal/now-instant (zolo-cal/date-string->instant "yyyy-MM-dd" "2012-12-21")]

      (db-assert/assert-datomic-suggestion-set-count 0)
      
      (let [shy (shy-persona/create)
            ss-set (ss-service/find-suggestion-set-for-today (:user/guid shy))]

        (db-assert/assert-datomic-suggestion-set-count 1)
        (db-assert/assert-datomic-contact-count 2)

        (is (not (nil? ss-set)))

        (is (= "ss-2012-12-21" (:suggestion-set/name ss-set)))

        (is (= 2 (count (:suggestion-set/contacts ss-set))))
        (is (= (set (map :contact/guid (:user/contacts shy)))
               (set (map :contact/guid (:suggestion-set/contacts ss-set))))))))

  (demonic-testing "Suggestion set is already created for today .. it should NOT create and return"
    (stubbing [zolo-cal/now-instant (zolo-cal/date-string->instant "yyyy-MM-dd" "2012-12-21")]

      (db-assert/assert-datomic-suggestion-set-count 0)
      
      (let [shy (shy-persona/create)
            ss-set (ss-service/find-suggestion-set-for-today (:user/guid shy))]

        (is (= ss-set
               (ss-service/find-suggestion-set-for-today (:user/guid shy))
               (ss-service/find-suggestion-set-for-today (:user/guid shy))))

        (db-assert/assert-datomic-suggestion-set-count 1)
        (db-assert/assert-datomic-contact-count 2)

        (is (not (nil? ss-set)))))))
