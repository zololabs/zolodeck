(ns zolo.api.message-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        conjure.core
        zolo.utils.clojure
        zolo.demonic.test
        zolo.demonic.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.personas.shy :as shy-persona]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.test.web-utils :as w-utils]
            [zolo.personas.generator :as pgen]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.test.assertions.datomic :as db-assert]))

(defn- messages-url [u c]
  (str "/users/" (or (:user/guid u) (random-guid-str))
       "/contacts/" (or (:contact/guid c) (random-guid-str))
       "/messages"))

(demonictest test-new-message
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
        jack (first (:user/contacts u))]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :post (messages-url u jack) {:text "Hey" :provider "facebook"})]
        (is (= 403 (:status resp)))))
        
    (testing "When user is not present it should return nil"
      (let [resp (w-utils/authed-request u  :post (messages-url "JUNK" jack) {})]
        (is (= 404 (:status resp)))))

    (testing "When contact is not present it should return nil"
      (let [resp (w-utils/authed-request u  :post (messages-url u "JUNK") {})]
        (is (= 404 (:status resp)))))

    (testing "When invalid message is send it return Bad Request"
      (let [resp (w-utils/authed-request u  :post (messages-url u jack) {})]
        (is (= 400 (:status resp)))))

    (stubbing [fb-chat/send-message true]
      (testing "Should call fb-chat send message with proper attributes and save temp message"
        (db-assert/assert-datomic-temp-message-count 0)

        (let [resp (w-utils/authed-request u  :post (messages-url u jack) {:text "Hey" :provider "facebook"})]
          (is (= 201 (:status resp))))

        (db-assert/assert-datomic-temp-message-count 1)))))
