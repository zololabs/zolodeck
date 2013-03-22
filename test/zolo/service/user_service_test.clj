(ns zolo.service.user-service-test
  (:use [clojure.test :only [deftest is are testing]]
        zolodeck.utils.debug
        zolodeck.demonic.test)
  (require [zolo.service.user-service :as u-service]
           [zolo.personas.factory :as personas]
           [zolo.test.assertions.datomic :as db-assert]
           [zolo.test.assertions.domain :as d-assert]
           [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

;; (deftest test-get-user

;;   (testing "when user is not present it should return nil"
;;     (is ))


;;   (testing "when user is present it should return distilled user"))


(deftest test-new-user
  (demonic-testing "new user sign up "
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]

       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-user-count 0)
       (db-assert/assert-datomic-user-identity-count 0)
       
       (let [distilled-mickey (u-service/new-user (personas/request-params mickey true))]
         (is (= "Mickey.Mouse@gmail.com" (:email distilled-mickey)))

         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1))))))
