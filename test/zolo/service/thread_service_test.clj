(ns zolo.service.thread-service-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.utils.clojure
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.service.thread-service :as t-service]
            [zolo.domain.thread :as t]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.utils.calendar :as zolo-cal]))

(deftest test-find-reply-to-threads

  (demonic-testing "User is not present, it should return nil"
    (is (nil? (t-service/find-threads nil t-service/REPLY-TO)))
    (is (nil? (t-service/find-threads (random-guid-str) t-service/REPLY-TO))))

  (demonic-testing "User present, but has no messages, it should return empty"
    (let [shy (shy-persona/create)
          threads (t-service/find-threads (:user/guid shy) t-service/REPLY-TO)]
      (is (empty? threads))))

  (demonic-testing "User has both a reply-to and a replied-to thread, it should return the reply-to thread"
    (let [vincent (vincent-persona/create)
          vincent-ui (-> vincent :user/user-identities first)
          vincent-uid (:identity/provider-uid vincent-ui)
          jack-ui (-> vincent :user/contacts second :contact/social-identities first)
          jack-uid (:social/provider-uid jack-ui)

          all-threads (t/messages->threads (:user/messages vincent))
          reply-threads (t-service/find-threads (:user/guid vincent) t-service/REPLY-TO)

          r-messages (-> reply-threads first :thread/messages)
          last-m (last r-messages)]

      (is (= 3 (count all-threads)))
      
      (is (= 1 (count reply-threads)))
      (is (-> reply-threads first :thread/guid))

      (let [lm-from-c (-> reply-threads first :thread/lm-from-contact)]
        (is (= (:social/first-name jack-ui) (:contact/first-name lm-from-c)))
        (is (= (:social/last-name jack-ui) (:contact/last-name lm-from-c)))
        (is (= (:social/photo-url jack-ui) (:contact/picture-url lm-from-c))))

      (let [reply-to-cs (-> reply-threads first :thread/reply-to-contacts)
            reply-to-c (first reply-to-cs)]
        (is (= 1 (count reply-to-cs)))
        (is (= (:social/first-name jack-ui) (:contact/first-name reply-to-c)))
        (is (= (:social/last-name jack-ui) (:contact/last-name reply-to-c)))
        (is (= (:social/photo-url jack-ui) (:contact/picture-url reply-to-c))))

      (is (= 1 (count r-messages)))
      (is (= jack-uid (:message/from last-m)))
      (is (= #{vincent-uid} (:message/to last-m)))
      (is (:message/snippet last-m)))))