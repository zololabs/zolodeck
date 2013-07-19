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
            [zolo.utils.calendar :as zolo-cal]
            [zolo.marconi.context-io.core :as e-lab]
            [zolo.service.distiller.thread :as t-distiller]))

(def THREAD-LIMIT 20)

(def THREAD-OFFSET 0)

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
           
           (let [all-t (it-> u (u-store/reload it) (t/all-threads it THREAD-LIMIT THREAD-OFFSET))
                 dt (it-> all-t (second it) (t-distiller/distill u it "include_messages"))]
             (has-keys dt [:thread/guid :thread/subject :thread/lm-from-contact :thread/provider :thread/messages :thread/ui-guid])
             (has-keys (:thread/lm-from-contact dt) [:contact/first-name :contact/last-name :contact/guid :contact/muted :contact/picture-url :contact/social-identities])
             (doseq [m (:thread/messages dt)]
               (has-keys m [:message/message-id :message/guid :message/provider :message/thread-id :message/from :message/to :message/date :message/text :message/snippet :message/sent :message/author :message/reply-to :message/ui-guid])
               (has-keys (:message/author m) [:author/first-name :author/last-name :author/picture-url])
               (doseq [r (:message/reply-to m)]
                 (has-keys r [:reply-to/first-name :reply-to/last-name :reply-to/provider-uid]))))))))))

(deftest test-mark-as-done
 
  (demonic-testing "User is not present, it should return nil"
    (is (nil? (t-service/update-thread-details nil "ui-guid" "message-id" true "2017-07-07T17:17:17.003Z")))
    (is (nil? (t-service/update-thread-details (random-guid-str) "ui-guid" "message-id" true "2017-07-07T17:17:17.003Z"))))

  (demonic-testing "UI is not present, it should return nil"
    (let [vincent (vincent-persona/create)]
      (is (nil? (t-service/update-thread-details (:user/guid vincent) nil  "message-id" true "2017-07-07T17:17:17.003Z")))
      (is (nil? (t-service/update-thread-details (:user/guid vincent) (random-guid-str) "message-id" true "2017-07-07T17:17:17.003Z")))))

  (demonic-testing "UI is from a different user, it should return nil"
    (let [vincent (vincent-persona/create)
          shy (shy-persona/create)
          vincent-ui-guid (-> vincent :user/user-identities first :identity/guid)
          m-id (-> vincent :user/messages last m/message-id)]
      (is (nil? (t-service/update-thread-details (:user/guid shy) vincent-ui-guid m-id true "2017-07-07T17:17:17.003Z")))))
  
  (demonic-testing "User present, but has no specified message, it should return empty"
    (let [vincent (vincent-persona/create)
          vincent-ui-guid (-> vincent :user/user-identities first :identity/guid)]
      (is (nil? (t-service/update-thread-details (:user/guid vincent) vincent-ui-guid "message-id" true "2017-07-07T17:17:17.003Z")))))

  (demonic-testing "Message should get updated"
    (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 3)]}})          
          u-ui-guid (-> u :user/user-identities first :identity/guid)
          m-id (->> u :user/messages (sort-by :message/date) first m/message-id)]
      (is (not (:thread/done (t-service/update-thread-details (:user/guid u) u-ui-guid m-id false "2017-07-07T17:17:17.003Z"))))      
      (is (:thread/done (t-service/update-thread-details (:user/guid u) u-ui-guid m-id true "2017-07-07T17:17:17.003Z"))))))


(demonictest test-load-thread-details
  (run-as-of "2012-05-08"
    (personas/in-email-lab
     (let [mickey-email "mickey@gmail.com"
           mickey (e-lab/create-account "Mickey" "Mouse" mickey-email)
           db-mickey (personas/create-db-user-from-email-user mickey)]
       
       (let [m1 (e-lab/send-message mickey-email "donald@gmail.com" "s1" "t1" "Hi, what's going on?" "2012-05-01 00:00")
             m2 (e-lab/send-message "donald@gmail.com" mickey-email "s2" "t1" "Nothing, just work." "2012-05-02 00:00")
             m3 (e-lab/send-message mickey-email "donald@gmail.com" "s3" "t1" "OK, groceries?" "2012-05-03 00:00")
             m10 (e-lab/send-message "daisy@gmail.com" mickey-email "s11" "t4" "Nothing, just work." "2012-05-04 00:00")
             m11 (e-lab/send-message mickey-email "daisy@gmail.com" "s12" "t4" "OK, groceries?" "2012-05-05 00:00")
             m12 (e-lab/send-message mickey-email "admin@thoughtworks.com" "s13" "t6" "Special Deal!" "2012-05-05 00:00")]
         
         (let [u (pgen/refresh-everything db-mickey)
               u-ui-guid (-> u :user/user-identities first :identity/guid)
               m-id (->> u :user/messages (sort-by :message/date) first m/message-id)]

           (testing "when zolo is upto date with social"
             (let [td (t-service/load-thread-details (:user/guid u) u-ui-guid m-id)]
               (is (= 3 (count (:thread/messages td))))
               (is (= ["s1" "s2" "s3"] (map :message/subject (sort-by m/message-date (:thread/messages td)))))))

           (testing "when new messages came to social layer and zolo hasnt refreshed"
             (let [m4 (e-lab/send-message mickey-email "donald@gmail.com" "s4" "t1" "yah sure" "2012-05-06 00:00")
                   td (t-service/load-thread-details (:user/guid u) u-ui-guid m-id)]
               (is (= 3 (count (:thread/messages td))))
               (is (= ["s1" "s2" "s3"] (map :message/subject (sort-by m/message-date (:thread/messages td)))))))

           (testing "when new messages came to social layer and zolo got refreshed after that"
             (let [u (pgen/refresh-everything u)
                   td (t-service/load-thread-details (:user/guid u) u-ui-guid m-id)]
               (is (= 4 (count (:thread/messages td))))
               (is (= ["s1" "s2" "s3" "s4"] (map :message/subject (sort-by m/message-date (:thread/messages td)))))))))))))
