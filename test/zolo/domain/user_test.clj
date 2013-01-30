(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

;;TODO Duplicate Function
(defn create-social-user [fb-user]
  (-> fb-user
      (personas/request-params true)
      (social/signup-user {})))

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

(deftest test-suggestion-set
  (demonic-integration-testing "Contact Count should not change when suggestion set changes"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           daisy (fb-lab/create-friend "Daisy" "Duck")
           minnie (fb-lab/create-friend "Minnie" "Mouse")
           db-mickey (in-demarcation (user/signup-new-user (create-social-user mickey)))]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey daisy)
       (fb-lab/make-friend mickey minnie)
       
       (fb-lab/login-as mickey)

       (in-demarcation
        (contact/update-contacts (user/reload db-mickey))
        (db-assert/assert-datomic-contact-count 3))

       (let [[db-daisy db-donald db-minnie] (in-demarcation
                                             (sort-by :contact/first-name (:user/contacts (in-demarcation (user/reload db-mickey)))))]
         (in-demarcation
          (user/new-suggestion-set (user/reload db-mickey) "2012-05-01" [db-daisy]))

         (in-demarcation
          (let [suggested-contacts (user/suggestion-set (user/reload db-mickey) "2012-05-01")]
            (is (= 1 (count suggested-contacts)) "Suggested only one contact ... so suggestion set should be 1")
            (d-assert/contacts-are-same daisy (first suggested-contacts))))


         (in-demarcation
          (user/new-suggestion-set (user/reload db-mickey) "2012-05-02" [db-minnie]))

         (in-demarcation
          (is (= 3 (count (:user/contacts (user/reload db-mickey)))) "No of contacts should not change"))

         (in-demarcation
          (let [suggested-contacts (user/suggestion-set (user/reload db-mickey) "2012-05-02")]
            (is (= 1 (count suggested-contacts)) "Suggested only one contact ... so suggestion set should be 1")
            (d-assert/contacts-are-same minnie (first suggested-contacts)))))))))

