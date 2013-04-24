(ns zolo.domain.thread-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.core :as d-core]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.thread :as t]
            [zolo.domain.message :as m]
            [zolo.personas.generator :as pgen]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]))

(deftest test-find-reply-to-threads

  (testing "When user does not have any messages it should return empty"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)]}})]
      (is (empty? (t/find-reply-to-threads u)))))

  (testing "When user has 1 thread with last message sent ... it should return empty"
    (doseq [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 8)]}
                                     :UI-IDS-ALLOWED [:FACEBOOK :EMAIL]
                                     :UI-IDS-COUNT 1})]
      (is (empty? (t/find-reply-to-threads u)))))

  (testing "When user has 1 thread with last message received ... it should return that thread"
    (doseq [u (pgen/generate-domain-all {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)]}
                                         :UI-IDS-ALLOWED [:FACEBOOK :EMAIL]
                                         :UI-IDS-COUNT 1})]
      (let [threads (t/find-reply-to-threads u)]
        (is (= 1 (count threads)))
        (is (-> threads first :thread/guid))
        (is (= (set (m/all-messages u)) (set (:thread/messages (first threads))))))))

    (testing "When user has 2 friends, with a reply-to and replied-to threads each, it should return reply-to"
      (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)
                                                       (pgen/create-friend-spec "Jim" "Beam" 1 10)]}})

            u-uid (-> u :user/user-identities first :identity/provider-uid)
            jack-uid (-> u :user/contacts second :contact/social-identities first :social/provider-uid)

            threads (t/find-reply-to-threads u)
            last-m (-> threads first :thread/messages last)]

        (is (= 1 (count threads)))
        (is (= #{u-uid} (:message/to last-m)))
        (is (= jack-uid (:message/from last-m)))))

    (testing "When user has 2 friends, with both replied-to threads, it should return empty"
      (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 20)
                                                       (pgen/create-friend-spec "Jim" "Beam" 1 10)]}})]
        
        (is (empty? (t/find-reply-to-threads u))))))