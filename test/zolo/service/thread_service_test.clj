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
            [zolo.domain.core :as d-core]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.utils.calendar :as zolo-cal]))
 
(deftest test-distilled-threads-with-temps
  (demonic-testing "When user has a thread with some temp-messages, distillation should still work"
    (d-core/run-in-gmt-tz
     (run-as-of "2012-05-12"
       (pgen/run-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 2)]}
                                     :UI-IDS-ALLOWED [:FACEBOOK]
                                     :UI-IDS-COUNT 1}
         (let [u-uid (-> u :user/user-identities first :identity/provider-uid)
               f-uid (-> u :user/contacts first
                         :contact/social-identities first :social/provider-uid)]
           (mocking [fb-chat/send-message]
             (m-service/new-message u {:text "Hey hello" :provider "facebook" :from u-uid :guid (-> u :user/guid str) :to [f-uid]}))
           
           (let [all-t (->> u u-store/reload t/all-threads)
                 dt (->> all-t second (t/distill u))]
             (has-keys dt [:thread/guid :thread/subject :thread/lm-from-contact :thread/provider :thread/messages :thread/ui-guid])
             (has-keys (:thread/lm-from-contact dt) [:contact/first-name :contact/last-name :contact/guid :contact/muted :contact/picture-url :contact/social-identities])
             (doseq [m (:thread/messages dt)]
               (has-keys m [:message/message-id :message/guid :message/provider :message/thread-id :message/from :message/to :message/date :message/text :message/snippet :message/sent :message/author :message/reply-to :message/ui-guid])
               (has-keys (:message/author m) [:author/first-name :author/last-name :author/picture-url])
               (doseq [r (:message/reply-to m)]
                 (has-keys r [:reply-to/first-name :reply-to/last-name :reply-to/provider-uid]))))))))))

(deftest test-mark-as-done
 
  (demonic-testing "User is not present, it should return nil"
    (is (nil? (t-service/update-thread-details nil "ui-guid" "message-id" true)))
    (is (nil? (t-service/update-thread-details (random-guid-str) "ui-guid" "message-id" true))))

  (demonic-testing "UI is not present, it should return nil"
    (let [vincent (vincent-persona/create)]
      (is (nil? (t-service/update-thread-details (:user/guid vincent) nil  "message-id" true)))
      (is (nil? (t-service/update-thread-details (:user/guid vincent) (random-guid-str) "message-id" true)))))

  (demonic-testing "UI is from a different user, it should return nil"
    (let [vincent (vincent-persona/create)
          shy (shy-persona/create)
          vincent-ui-guid (-> vincent :user/user-identities first :identity/guid)
          m-id (-> vincent :user/messages last m/message-id)]
      (is (nil? (t-service/update-thread-details (:user/guid shy) vincent-ui-guid m-id true)))))
  
  (demonic-testing "User present, but has no specified message, it should return empty"
    (let [vincent (vincent-persona/create)
          vincent-ui-guid (-> vincent :user/user-identities first :identity/guid)]
      (is (nil? (t-service/update-thread-details (:user/guid vincent) vincent-ui-guid "message-id" true)))))

  (demonic-testing "Message should get updated"
    (let [vincent (vincent-persona/create)
          vincent-ui-guid (-> vincent :user/user-identities first :identity/guid)
          m-id (-> vincent :user/messages last m/message-id)]
      (is (not (:message/done (t-service/update-thread-details (:user/guid vincent) vincent-ui-guid m-id false))))      
      (is (:message/done (t-service/update-thread-details (:user/guid vincent) vincent-ui-guid m-id true))))))