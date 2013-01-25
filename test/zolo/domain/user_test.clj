(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.test.assertions.datomic :as db-assert]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

(deftest test-signup-new-user
  (demonic-testing "First time user"
    (fb-lab/in-facebook-lab
     (let [db-user (personas/create-new-db-user "First" "Time")]
       (db-assert/assert-datomic-id-present db-user)
       (db-assert/assert-datomic-user-count 1)
       (db-assert/assert-datomic-user-identity-count 1)))))

(deftest test-find-all-users-for-refresh
  (demonic-testing "When 1 of 2 users have granted permissions"
    (fb-lab/in-facebook-lab
     (let [granted-user (personas/create-new-db-user "Permission" "Granted")
           not-granted-user (personas/create-new-db-user "Permission" "NOTGranted" false)]
       (let [users (filter :user-temp/fb-permissions-time (user/find-all-users-for-refreshes))]
         (is (= 1 (count users)))
         (is (= (:user/guid granted-user) (:user/guid (first users)))))))))

