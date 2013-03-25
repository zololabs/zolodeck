(ns zolo.service.user-service-test
  (:use [clojure.test :only [deftest is are testing]]
        zolodeck.utils.debug
        zolodeck.utils.clojure
        zolodeck.demonic.test)
  (require [zolo.service.user-service :as u-service]
           [zolo.domain.user-identity :as user-identity]
           [zolo.store.user-store :as u-store]
           [zolo.personas.factory :as personas]
           [zolo.test.assertions.datomic :as db-assert]
           [zolo.test.assertions.domain :as d-assert]
           [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

(deftest test-get-users
  (demonic-testing "when user is not present it should return nil"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]
       (is (nil? (u-service/get-users (personas/request-params mickey true)))))))

  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey1 (u-service/new-user (personas/request-params mickey true))
           d-mickey2 (u-service/get-users (personas/request-params mickey true))]

       (is (= d-mickey1 d-mickey2))))))

(deftest test-get-user-by-guid
  (demonic-testing "when user is not present it should return nil"
    (personas/in-social-lab
     (is (nil? (u-service/get-user-by-guid (random-guid-str))))))

  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey1 (u-service/new-user (personas/request-params mickey true))
           d-mickey2 (u-service/get-user-by-guid (:user/guid d-mickey1))]

       (is (= d-mickey1 d-mickey2))))))

(deftest test-update-user
  (demonic-testing "when user is not present, it should return nil"
    (is (nil? (u-service/update-user (zolodeck.utils.clojure/random-guid-str) {}))))

  (demonic-testing "when user is present, it should return updated distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]

       (fb-lab/login-as mickey)
       
       (let [mickey-guid (:user/guid (u-service/new-user (personas/request-params mickey false)))
             db-mickey-1 (u-store/find-by-guid mickey-guid)
             _ (u-service/update-user mickey-guid (personas/request-params mickey true))
             db-mickey-2 (u-store/find-by-guid mickey-guid)]

         (is (not (user-identity/fb-permissions-granted? db-mickey-1)))
         (is (user-identity/fb-permissions-granted? db-mickey-2))

         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1))))))


(deftest test-new-user
  (demonic-testing "new user sign up "
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]

       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-user-count 0)
       (db-assert/assert-datomic-user-identity-count 0)
       
       (let [distilled-mickey (u-service/new-user (personas/request-params mickey true))]
         (is (= "Mickey.Mouse@gmail.com" (:user/email distilled-mickey)))

         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1))))))
