(ns zolo.domain.interaction-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.domain.message :as message]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.accessors :as dom]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.domain.contact :as contact]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]))

(deftest test-interactions-by-contacts
  (testing "when no messages are present"
    (let [shy (shy-persona/create-domain)
          imbc (message/inbox-messages-by-contacts shy)
          ibc (interaction/interactions-by-contacts imbc)]

      (is (not (nil? ibc)))

      (is (= 2 (count ibc)))

      (is (= (set (:user/contacts shy)) (set (keys ibc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))]
        (is (empty? (ibc jack)))
        (is (empty? (ibc jill))))))

  (testing "when messages are present"
    (let [vincent (vincent-persona/create-domain)
          imbc (message/inbox-messages-by-contacts vincent)
          ibc (interaction/interactions-by-contacts imbc)]
      
      (is (not (nil? ibc)))

      (is (= 2 (count ibc)))

      (is (= (set (:user/contacts vincent)) (set (keys ibc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts vincent))]
        (is-not (empty? (ibc jack)))
        (is (= 2 (count (ibc jack))))
        
        (is-not (empty? (ibc jill)))
        (is (= 1 (count (ibc jill))))))))

;; (deftest test-update-inbox-messages
;;   (demonic-integration-testing  "First time user"
;;     (personas/in-social-lab
;;      (let [mickey (fb-lab/create-user "Mickey" "Mouse")
;;            donald (fb-lab/create-friend "Donald" "Duck")
;;            daisy (fb-lab/create-friend "Daisy" "Duck")
;;            db-mickey (in-demarcation (user/signup-new-user (personas/create-social-user mickey)))]
;;        (fb-lab/make-friend mickey donald)
;;        (fb-lab/make-friend mickey daisy)

;;        (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
;;              m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
;;              m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
             
;;              m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
;;              m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
         
;;          (fb-lab/login-as mickey)

;;          (in-demarcation
;;           (db-assert/assert-datomic-message-count 0))

;;          (in-demarcation
;;           (contact/update-contacts (user/reload db-mickey))
;;           (message/update-inbox-messages (user/reload db-mickey))
;;           (db-assert/assert-datomic-message-count 5))

;;          (in-demarcation)
;;          (let [[dm1 dm2 dm3 dm4 dm5] (sort-by dom/message-date (:user/messages (in-demarcation (user/reload db-mickey))))]
;;            (d-assert/messages-are-same m1 dm1)
;;            (d-assert/messages-are-same m2 dm2)
;;            (d-assert/messages-are-same m3 dm3)
;;            (d-assert/messages-are-same m4 dm4)
;;            (d-assert/messages-are-same m5 dm5)))))))


;; (deftest test-update-feed-messages-for-contact
;;   (demonic-integration-testing "Feeds should be updated"
;;     (personas/in-social-lab
;;      (let [mickey (fb-lab/create-user "Mickey" "Mouse")
;;            donald (fb-lab/create-friend "Donald" "Duck")
;;            daisy (fb-lab/create-friend "Daisy" "Duck")
;;            db-mickey (in-demarcation (user/signup-new-user (personas/create-social-user mickey)))]

;;        (fb-lab/make-friend mickey donald)
;;        (fb-lab/make-friend mickey daisy)
       
;;        (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
;;              m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
;;              m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
             
;;              m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
;;              m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
         
;;          (fb-lab/login-as mickey)

;;          (in-demarcation
;;           (db-assert/assert-datomic-message-count 0))

;;          (in-demarcation
;;           (contact/update-contacts (user/reload db-mickey))
;;           (message/update-inbox-messages (user/reload db-mickey))
;;           (db-assert/assert-datomic-message-count 5))

;;          (in-demarcation)
;;          (let [[dm1 dm2 dm3 dm4 dm5] (sort-by dom/message-date (:user/messages (in-demarcation (user/reload db-mickey))))]
;;            (d-assert/messages-are-same m1 dm1)
;;            (d-assert/messages-are-same m2 dm2)
;;            (d-assert/messages-are-same m3 dm3)
;;            (d-assert/messages-are-same m4 dm4)
;;            (d-assert/messages-are-same m5 dm5)))))))