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

(def REPLY-TO "reply_to")

(deftest test-find-reply-to-threads

  (demonic-testing "User is not present, it should return nil"
    (is (nil? (t-service/find-threads nil REPLY-TO)))
    (is (nil? (t-service/find-threads (random-guid-str) REPLY-TO))))

  (demonic-testing "User present, but has no messages, it should return empty"
    (let [shy (shy-persona/create)
          threads (t-service/find-threads (:user/guid shy) REPLY-TO)]
      (is (empty? threads))))

  (demonic-testing "User has both a reply-to and a replied-to thread, it should return the reply-to thread"
    (let [vincent (vincent-persona/create)
          vincent-uid (-> vincent :user/user-identities first :identity/provider-uid)
          jack-uid (-> vincent :user/contacts second :contact/social-identities first :social/provider-uid)

          all-threads (t/messages->threads (:user/messages vincent))
          reply-threads (t-service/find-threads (:user/guid vincent) REPLY-TO)

          r-messages (-> reply-threads first :thread/messages)
          last-m (last r-messages)]

      (is (= 3 (count all-threads)))

      (is (= 1 (count reply-threads)))
      (is (= 1 (count r-messages)))
      (is (= jack-uid (:message/from last-m)))
      (is (= #{vincent-uid} (:message/to last-m))))))