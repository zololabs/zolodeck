(ns zolo.domain.thread-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.test.assertions.core
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

(deftest test-distilled-threads
  (testing "When user has a thread with all regular messages, distillation should work"
    (d-core/run-in-gmt-tz
     (run-as-of "2012-05-12"
       (pgen/run-demarcated-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)]}
                                                :UI-IDS-ALLOWED [:FACEBOOK]
                                                :UI-IDS-COUNT 1}
                                             (let [dt (->> u t/all-threads first (t/distill u))]
                                               (has-keys dt [:thread/guid :thread/subject :thread/lm-from-contact :thread/provider :thread/messages])
                                               (has-keys (:thread/lm-from-contact dt) [:contact/first-name :contact/last-name :contact/guid :contact/muted :contact/picture-url :contact/social-identities])
                                               (doseq [m (:thread/messages dt)]
                                                 (has-keys m [:message/message-id :message/guid :message/provider :message/thread-id :message/from :message/to :message/date :message/text :message/snippet :message/sent :message/author :message/reply-to])
                                                 (has-keys (:message/author m) [:author/first-name :author/last-name :author/picture-url])
                                                 (doseq [r (:message/reply-to m)]
                                                   (has-keys r [:reply-to/first-name :reply-to/last-name :reply-to/provider-uid])))))))))

(deftest test-find-reply-to-threads
  (run-as-of "2012-05-12"
             (testing "When user does not have any messages it should return empty"
               (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)]}})]
                 (is (empty? (t/find-reply-to-threads u)))))

             (testing "When user has 1 thread with last message sent ... it should return empty"
               (pgen/run-demarcated-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 8)]}
                                                        :UI-IDS-ALLOWED [:FACEBOOK]
                                                        :UI-IDS-COUNT 1}
                 (is (empty? (t/find-reply-to-threads u)))))

             (testing "When user has 1 thread with last message received ... it should return that thread"
               (pgen/run-demarcated-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)]}
                                             :UI-IDS-ALLOWED [:FACEBOOK ]
                                             :UI-IDS-COUNT 1}
                 (let [threads (t/find-reply-to-threads u)]
                   (is (= 1 (count threads)))
                   (is (-> threads first :thread/guid))
                   (is (= (set (m/all-messages u)) (set (:thread/messages (first threads))))))))

             (testing "When user has 2 friends, with a reply-to and replied-to threads each, it should return reply-to"
               (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)
                                                                (pgen/create-friend-spec "Jim" "Beam" 1 10)]}})
                     u-uid (-> u :user/user-identities first :identity/provider-uid)

                     [jack jim] (->> u :user/contacts (sort-by :contact/first-name))
                     
                     jack-uid (-> jack :contact/social-identities first :social/provider-uid)

                     threads (t/find-reply-to-threads u)
                     last-m (-> threads first :thread/messages first)]

                 (is (= 1 (count threads)))
                 (is (= #{u-uid} (:message/to last-m)))
                 (is (= jack-uid (:message/from last-m)))))

             (testing "When user has 2 friends, with both replied-to threads, it should return empty"
               (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 20)
                                                                (pgen/create-friend-spec "Jim" "Beam" 1 10)]}})]
                 
                 (is (empty? (t/find-reply-to-threads u)))))))

(deftest test-find-follow-up-threads
  (run-as-of "2012-05-12"
             (testing "When user does not have any messages it should return empty"
               (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)]}})]
                 (is (empty? (t/find-follow-up-threads u)))))
             
             (testing "When user has 1 thread with last message received ... it should return empty"
               (pgen/run-demarcated-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)]}
                                                      :UI-IDS-ALLOWED [:FACEBOOK]
                                                      :UI-IDS-COUNT 1}
                 (is (empty? (t/find-follow-up-threads u)))))

             (testing "When user has 1 thread with last message sent ... it should return that thread"
               (pgen/run-demarcated-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 8)]}
                                             :UI-IDS-ALLOWED [:FACEBOOK]
                                             :UI-IDS-COUNT 1}
                 (let [threads (t/find-follow-up-threads u)]
                   (is (= 1 (count threads)))
                   (is (-> threads first :thread/guid))
                   (is (= (set (m/all-messages u)) (set (:thread/messages (first threads))))))))

             (testing "When user has 2 friends, with a reply-to and replied-to threads each, it should return reply-to"
               (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)
                                                                (pgen/create-friend-spec "Jim" "Beam" 1 10)]}})
                     
                     u-uid (-> u :user/user-identities first :identity/provider-uid)

                     [jack jim] (->> u :user/contacts (sort-by :contact/first-name))
                     jim-uid (-> jim :contact/social-identities first :social/provider-uid)

                     threads (t/find-follow-up-threads u)
                     last-m (-> threads first :thread/messages first)]

                 (is (= 1 (count threads)))
                 (is (= #{jim-uid} (:message/to last-m)))
                 (is (= u-uid (:message/from last-m)))))))
