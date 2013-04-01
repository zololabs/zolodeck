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
            [zolo.domain.user-identity :as user-identity]
            [zolo.domain.contact :as contact]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]))

;; (deftest test-update
;;   (demonic-testing "User is not found"
;;     (is (thrown-with-msg? RuntimeException #"User should not be nil"
;;           (user/update nil {:user/first-name "dummy"}))))
  
;;   (demonic-testing "User is present with contacts, messages, user-identities, suggestion-set"
    
;;     (testing "login info should be present")
;;     (testing "login tz should be present")
;;     (testing "first name and last name ")
;;     (testing "user-identities, contacts , messages , suggestion-set")))

;; (deftest test-signup-new-user
;;   (demonic-testing "First time user"
;;     (fb-lab/in-facebook-lab
;;      (let [db-user (personas/create-new-db-user "First" "Time")]
;;        (db-assert/assert-datomic-id-present db-user)
;;        (db-assert/assert-datomic-user-count 1)
;;        (db-assert/assert-datomic-user-identity-count 1)))))

;; (deftest test-update-creds
;;   (demonic-integration-testing "Contact Count should not change when user logs in again"
;;     (personas/in-social-lab
;;      (let [mickey (fb-lab/create-user "Mickey" "Mouse")
;;            donald (fb-lab/create-friend "Donald" "Duck")
;;            daisy (fb-lab/create-friend "Daisy" "Duck")
;;            minnie (fb-lab/create-friend "Minnie" "Mouse")
;;            db-mickey (in-demarcation (user/signup-new-user (personas/create-social-user mickey)))]

;;        (fb-lab/make-friend mickey donald)
;;        (fb-lab/make-friend mickey daisy)
;;        (fb-lab/make-friend mickey minnie)
       
;;        (fb-lab/login-as mickey)

;;        (in-demarcation
;;         (contact/update-contacts (user/reload db-mickey))
;;         (db-assert/assert-datomic-contact-count 3))

;;        (in-demarcation
;;         (is (= 3 (count (:user/contacts (user/reload db-mickey)))) "No of contacts should not change"))

;;        (dotimes [n 100]
;;          (in-demarcation
;;           (user/update-creds (user/reload db-mickey) {:access-token "1000"})
;;           (user/update-permissions-granted (user/reload db-mickey) ([true false] (rand-int 2))))

;;          (in-demarcation
;;           (is (= 3 (count (:user/contacts (user/reload db-mickey)))) "No of contacts should not change")
;;           (is (= 1 (count (versions (user/reload db-mickey) :user/contacts))))))))))

;; (deftest test-find-all-users-for-refresh
;;   (demonic-testing "When 1 of 2 users have granted permissions"
;;     (fb-lab/in-facebook-lab
;;      (let [granted-user (personas/create-new-db-user "Permission" "Granted")
;;            not-granted-user (personas/create-new-db-user "Permission" "NOTGranted" false)]
;;        (let [users (filter :user-temp/fb-permissions-time (user/find-all-users-for-refreshes))]
;;          (is (= 1 (count users)))
;;          (is (= (:user/guid granted-user) (:user/guid (first users)))))))))


(deftest test-update-with-extended-fb-auth-token
  (personas/in-social-lab
   (testing "User has no FB ui"
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey (-> mickey
                        personas/create-domain-user
                        (dissoc :user/user-identities))
           u-mickey (user/update-with-extended-fb-auth-token d-mickey "new-token")]
       
       (is (= d-mickey u-mickey))
       (is (nil? (user-identity/fb-access-token u-mickey)))))

   (testing "User has FB ui"
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey (personas/create-domain-user mickey)
           u-mickey (user/update-with-extended-fb-auth-token d-mickey "new-token")]

       (is (not= d-mickey u-mickey))
       (is (= "new-token" (user-identity/fb-access-token u-mickey)))))))

(deftest test-update-tz-offset
  (let [mickey {}]
    
    (is (nil? (:user/login-tz mickey)))

    (is (nil? (:user/login-tz (user/update-tz-offset mickey nil))))

    (is (= 420 (:user/login-tz (user/update-tz-offset mickey 420))))))

(deftest test-distill
  (testing "Should return nil when Nil is passed"
    (is (nil? (user/distill nil))))
  
  (testing "Email should be empty if no user-identities are present"
    (let [du (user/distill {:user/guid "abc"})]
      (is (= "abc" (:user/guid du)))
      (is (empty? (:user/email du)))))

  (testing "Login tz should be returned properly"
    (is (= 420 (:user/login-tz (user/distill {:user/login-tz 420}))))
    (is (nil? (:user/login-tz (user/distill {:user/guid "abc"})))))

  (testing "Updated flag should be returned properly"
    (is (:user/updated (user/distill {:user/last-updated "sometime"})))
    (is (not (:user/updated (user/distill {:user/guid "abc"})))))
  
  (demonic-testing "Should return properly distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey (personas/create-domain-user mickey)
           du (user/distill d-mickey)]
       (is (= (str (:user/guid d-mickey)) (:user/guid du)))
       (is (= (user-identity/fb-email d-mickey) (:user/email du)))))))