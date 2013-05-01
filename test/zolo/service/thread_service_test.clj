(ns zolo.service.thread-service-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.utils.clojure
        zolo.test.core-utils
        zolo.test.assertions.core
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.personas.generator :as pgen]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.service.thread-service :as t-service]
            [zolo.domain.message :as m]
            [zolo.service.message-service :as m-service]
            [zolo.store.user-store :as u-store]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.domain.thread :as t]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.utils.calendar :as zolo-cal]))


(deftest test-distilled-threads-with-temps
  (demonic-testing "When user has a thread with some temp-messages, distillation should still work"
    (doseq [u (pgen/generate-all {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 2)]}
                                         :UI-IDS-ALLOWED [:FACEBOOK :EMAIL]
                                         :UI-IDS-COUNT 1})]
      (let [f-uid (-> u :user/contacts first :contact/social-identities first :social/provider-uid)]
        (mocking [fb-chat/send-message]
          (m-service/new-message u {:text "Hey hello" :provider "facebook" :guid (-> u :user/guid str) :to [f-uid]}))
        (let [dt (->> u u-store/reload t/all-threads second (t/distill u))]
          (has-keys dt [:thread/guid :thread/subject :thread/lm-from-contact :thread/provider :thread/messages])
          (has-keys (:thread/lm-from-contact dt) [:contact/first-name :contact/last-name :contact/guid :contact/muted :contact/picture-url :contact/social-identities])
          (doseq [m (:thread/messages dt)]
            (has-keys m [:message/message-id :message/guid :message/provider :message/thread-id :message/from :message/to :message/date :message/text :message/snippet :message/sent :message/author :message/reply-to])
            (has-keys (:message/author m) [:author/first-name :author/last-name :author/picture-url])
            (doseq [r (:message/reply-to m)]
              (has-keys r [:reply-to/first-name :reply-to/last-name :reply-to/provider-uid]))))))))

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

          all-threads (t/messages->threads vincent (:user/messages vincent))
          reply-threads (t-service/find-threads (:user/guid vincent) t-service/REPLY-TO)

          r-messages (-> reply-threads first :thread/messages)
          last-m (first r-messages)]

      (is (= 3 (count all-threads)))
      
      (is (= 1 (count reply-threads)))
      (is (= (str "Conversation with " (:social/first-name jack-ui) " " (:social/last-name jack-ui))
             (-> reply-threads first :thread/subject)))
      
      (is (= 1 (count r-messages)))
      (is (= jack-uid (:message/from last-m)))
      (is (= #{vincent-uid} (:message/to last-m)))
      (is (:message/snippet last-m))
      (is-not (:message/sent last-m))

      (let [lm-from-c (-> reply-threads first :thread/lm-from-contact)]
        (is (= (:social/first-name jack-ui) (:contact/first-name lm-from-c)))
        (is (= (:social/last-name jack-ui) (:contact/last-name lm-from-c)))
        (is (= (:social/photo-url jack-ui) (:contact/picture-url lm-from-c))))

      (let [author (:message/author last-m)]
        (is (= (:social/first-name jack-ui) (:author/first-name author)))
        (is (= (:social/last-name jack-ui) (:author/last-name author)))
        (is (= (:social/photo-url jack-ui) (:author/picture-url author))))

      (let [reply-tos (:message/reply-to last-m)
            reply-to (first reply-tos)]
        (is (= (:social/first-name jack-ui) (:reply-to/first-name reply-to)))
        (is (= (:social/last-name jack-ui) (:reply-to/last-name reply-to)))
        (is (= (:social/provider-uid jack-ui) (:reply-to/provider-uid reply-to))))

      (testing "after replying to this thread, it shouldn't be a reply-to thread"
        (let [r-message (first r-messages)
              _ (mocking [fb-chat/send-message]
                  (m-service/new-message vincent {:text "Hey hello" :provider "facebook"
                                                  :guid (-> vincent :user/guid str)
                                                  :to [(-> r-message :message/reply-to first :reply-to/provider-uid)]
                                                  :thread_id (:message/thread-id r-message)}))
              updated-r-threads (t-service/find-threads (:user/guid vincent) t-service/REPLY-TO)]
          (is (= 1 (count reply-threads)))
          (is (empty? updated-r-threads)))))))

(deftest test-find-follow-up-threads

  (demonic-testing "User is not present, it should return nil"
    (is (nil? (t-service/find-threads nil t-service/FOLLOW-UP)))
    (is (nil? (t-service/find-threads (random-guid-str) t-service/FOLLOW-UP))))

  (demonic-testing "User present, but has no messages, it should return empty"
    (let [shy (shy-persona/create)
          threads (t-service/find-threads (:user/guid shy) t-service/FOLLOW-UP)]
      (is (empty? threads))))

  (demonic-testing "User has both a reply-to and a replied-to thread, it should return the reply-to thread"
    (let [vincent (vincent-persona/create)
          vincent-ui (-> vincent :user/user-identities first)
          vincent-uid (:identity/provider-uid vincent-ui)

          jill-ui (-> vincent :user/contacts first :contact/social-identities first)
          jill-uid (:social/provider-uid jill-ui)

          jack-ui (-> vincent :user/contacts second :contact/social-identities first)
          jack-uid (:social/provider-uid jack-ui)

          all-threads (t/messages->threads vincent (:user/messages vincent))
          follow-threads (t-service/find-threads (:user/guid vincent) t-service/FOLLOW-UP)

          jill-messages (-> follow-threads first :thread/messages)          
          jack-messages (-> follow-threads second :thread/messages)

          jack-last-m (first jack-messages)
          jill-last-m (first jill-messages)]

      (is (= 3 (count all-threads)))
      
      (is (= 2 (count follow-threads)))
      (is (= (str "Conversation with " (:social/first-name jill-ui) " " (:social/last-name jill-ui))
             (-> follow-threads first :thread/subject)))
      (is (= (str "Conversation with " (:social/first-name jack-ui) " " (:social/last-name jack-ui))
             (-> follow-threads second :thread/subject)))
      
      (is (= 2 (count jack-messages)))
      (is (= 2 (count jill-messages)))
      (is (= vincent-uid (:message/from jack-last-m)))
      (is (= #{jack-uid} (:message/to jack-last-m)))
      (is (:message/snippet jack-last-m))
      (is (:message/sent jack-last-m))

      (let [lm-from-c (-> follow-threads second :thread/lm-from-contact)]
        (is (= (:social/first-name jack-ui) (:contact/first-name lm-from-c)))
        (is (= (:social/last-name jack-ui) (:contact/last-name lm-from-c)))
        (is (= (:social/photo-url jack-ui) (:contact/picture-url lm-from-c))))

      (let [author (:message/author jack-last-m)]
        (is (= (:identity/first-name vincent-ui) (:author/first-name author)))
        (is (= (:identity/last-name vincent-ui) (:author/last-name author)))
        (is (= (:identity/photo-url vincent-ui) (:author/picture-url author))))

      (let [reply-tos (:message/reply-to jack-last-m)
            reply-to (first reply-tos)]
        (is (= (:social/first-name jack-ui) (:reply-to/first-name reply-to)))
        (is (= (:social/last-name jack-ui) (:reply-to/last-name reply-to)))
        (is (= (:social/provider-uid jack-ui) (:reply-to/provider-uid reply-to)))))))