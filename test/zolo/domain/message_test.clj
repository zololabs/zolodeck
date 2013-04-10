(ns zolo.domain.message-test
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
            [zolo.domain.accessors :as dom]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.domain.contact :as contact]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.core :as d-core]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.personas.generator :as pgen]))

(deftest test-is-inbox-message
  (testing "When nil is passed return false"
    (is (not (message/is-inbox-message? nil))))

  (testing "When not inbox message it should return false"
    (is (not (message/is-inbox-message? {:message/mode "FEED"})))
    (is (not (message/is-inbox-message? {:message/mode "JUNK"}))))

  (testing "When inbox message it should return true"
    (is (message/is-inbox-message? {:message/mode "INBOX"})))

  (testing "When temp message it should return true"
    (is (message/is-inbox-message? {:temp-message/guid "abc"}))))

(deftest test-inbox-messages-by-contacts
  (testing "when no messages are present"
    (let [shy (shy-persona/create-domain)
          imbc (message/inbox-messages-by-contacts shy)]

      (is (not (nil? imbc)))

      (is (= 2 (count imbc)))

      (is (= (set (:user/contacts shy)) (set (keys imbc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))]
        (is (empty? (imbc jack)))
        (is (empty? (imbc jill))))))

  (testing "when messages are present"
    (let [vincent (vincent-persona/create-domain)
          imbc (message/inbox-messages-by-contacts vincent)]
      
      (is (not (nil? imbc)))

      (is (= 2 (count imbc)))

      (is (= (set (:user/contacts vincent)) (set (keys imbc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts vincent))]
        (is-not (empty? (imbc jack)))
        (is (= 3 (count (imbc jack))))
        
        (is-not (empty? (imbc jill)))
        (is (= 2 (count (imbc jill))))))))

(deftest test-last-sent-message
  (testing "When there is no messages it should return nil"
    (let [u (pgen/generate-domain {:FACEBOOK {:friends [(pgen/create-friend-spec "Jack" "Daniels")]}})
          jack (first (:user/contacts u))]
      (is (nil? (message/last-sent-message jack nil)))
      (is (nil? (message/last-sent-message jack [])))))

  (testing "When there is no sent messages it should return nil"
    (let [u (pgen/generate-domain {:FACEBOOK {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
          jack (first (:user/contacts u))
          msgs (-> u interaction/ibc interaction/messages-from-ibc)]
      (is (nil? (message/last-sent-message jack msgs)))))

  (testing "When only temp message is present it should return the last temp message")

  (testing "When only regular message is present it should return the last regular message"
    (d-core/run-in-gmt-tz
      (let [u (pgen/generate-domain {:FACEBOOK {:friends [(pgen/create-friend-spec "Jack" "Daniels" 5 15)]}})
            jack (first (:user/contacts u))
            msgs (-> u interaction/ibc interaction/messages-from-ibc)
            l-msg (message/last-sent-message jack msgs)]
        (is (not (nil? l-msg)))
        (is (= #inst "2012-05-14T00:00:00.000000000-00:00" (message/message-date l-msg))))))

  (testing "When temp and regular messages are present it should return the last sent message")) 

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