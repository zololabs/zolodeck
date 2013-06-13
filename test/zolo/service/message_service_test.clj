(ns zolo.service.message-service-test
  (:use zolo.utils.debug
        zolo.utils.clojure
        clojure.test
        conjure.core
        zolo.test.assertions.core
        zolo.demonic.test)
  (:require [zolo.personas.factory :as personas]
            [zolo.personas.vincent :as vincent-persona]            
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolo.domain.user-identity :as user-identity]
            [zolo.store.user-store :as u-store]
            [zolo.store.message-store :as m-store]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.social.email.gateway :as e-gateway]
            [zolo.personas.generator :as pgen]))

(demonictest test-update-inbox-messages
  (testing "when user is not present, it should return nil"
    (is (nil? (m-service/update-inbox-messages nil))))
    
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-friend "Donald" "Duck")
         daisy (fb-lab/create-friend "Daisy" "Duck")
         db-mickey (personas/create-db-user-from-fb-user mickey)]

     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01 00:00")
           m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02 00:00")
           m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03 00:00")]
       
       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-message-count 0)

       (testing  "User with no previous messages"
         (let [refreshed-mickey (-> db-mickey
                                    c-service/update-contacts-for-user
                                    m-service/update-inbox-messages)
               ui (-> refreshed-mickey :user/user-identities first)
               ui-db-id (:db/id ui)
               refreshed-messages (->> refreshed-mickey
                                       :user/messages
                                       (sort-by message/message-date))]

           (db-assert/assert-datomic-message-count 3)
           (d-assert/messages-list-are-same [m1 m2 m3] refreshed-messages)
           (doseq [m refreshed-messages]
             (is (= ui-db-id (-> m :message/user-identity :db/id))))))

       (testing "When previous messages are present it should not override"

           (fb-lab/remove-all-messages mickey)
           
           (let [m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01 00:00")
                 m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02 00:00")]
         
             (db-assert/assert-datomic-message-count 3)

             (let [refreshed-mickey (-> db-mickey
                                        c-service/update-contacts-for-user
                                        m-service/update-inbox-messages)]

               (db-assert/assert-datomic-message-count 5)
               (d-assert/messages-list-are-same [m1 m2 m3 m4 m5] (->> refreshed-mickey
                                                                      :user/messages
                                                                      (sort-by message/message-date)))))))))

  (demonic-testing "User who has temp messages should get deleted after message update"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           db-mickey (personas/create-db-user-from-fb-user mickey)]
       
       (fb-lab/make-friend mickey donald)
       
       (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01 00:00")
             tm1 (personas/create-temp-message db-mickey "to-uid1" "Hello")
             u-db-mickey (m-store/append-temp-message db-mickey tm1)]
       
       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-temp-message-count 1)

       (testing  "User with no previous messages"
         (let [refreshed-mickey (-> u-db-mickey
                                    c-service/update-contacts-for-user
                                    m-service/update-inbox-messages)]

           (db-assert/assert-datomic-temp-message-count 0))))))))


(demonictest test-new-message-from-facebook
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
        u-uid (-> u :user/user-identities first :identity/provider-uid)
        u-at (-> u :user/user-identities first :identity/auth-token)
        u-guid (-> u :user/guid str)
        [jack] (sort-by contact/first-name (:user/contacts u))
        jack-uid (-> jack :contact/social-identities first :social/provider-uid)]

    (testing "When user is not present it should return nil"
      (is (nil? (m-service/new-message nil {:from u-uid :text "hey" :provider "facebook" :guid u-guid :to [jack-uid]}))))
    
    (testing "When invalid message is send it should throw exception"
      (is (thrown-with-msg? RuntimeException #"bad-request"
            (m-service/new-message u {:text "" :provider "facebook" :guid u-guid :to [jack-uid]})))
      (is (thrown-with-msg? RuntimeException #"bad-request"
            (m-service/new-message u {:text "Hey" :provider ""  :guid u-guid :to [jack-uid]}))))
    
    (mocking [fb-chat/send-message]
      (testing "Should call fb-chat send message with proper attributes and save temp message"
        (db-assert/assert-datomic-temp-message-count 0)

        (m-service/new-message u {:from u-uid :text "How you doing?" :provider "facebook" :guid u-guid :to [jack-uid]})

        (let [updated-u (u-store/reload u)]
          
          (verify-call-times-for fb-chat/send-message 1)
          (verify-first-call-args-for fb-chat/send-message u-uid u-at [(contact/provider-id jack :provider/facebook)] "How you doing?")
          
          (db-assert/assert-datomic-temp-message-count 1)
          
          (is (= 1 (count (:user/temp-messages updated-u))))
          (is (= "How you doing?" (-> updated-u :user/temp-messages first :temp-message/text))))))))


(demonictest test-new-message-from-email
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}
                          :UI-IDS-ALLOWED [:EMAIL]
                          :UI-IDS-COUNT 1})
        u-uid (-> u :user/user-identities first :identity/provider-uid)
        u-email (-> u :user/user-identities first :identity/email)
        u-at (-> u :user/user-identities first :identity/auth-token)
        u-guid (-> u :user/guid str)
        [jack] (sort-by contact/first-name (:user/contacts u))
        jack-uid (-> jack :contact/social-identities first :social/provider-uid)]

    (mocking [e-gateway/send-email]
      (testing "Should call send-email with proper attributes and save temp message"
        (db-assert/assert-datomic-temp-message-count 0)

        (m-service/new-message u {:from u-email :subject "Hi" :text "How you doing?" :provider "email" :guid u-guid :to ["jack@email.com"] :reply_to_message_id "123123"})
        (let [updated-u (u-store/reload u)]
          
          (verify-call-times-for e-gateway/send-email 1)
          (verify-first-call-args-for e-gateway/send-email u-at u-email ["jack@email.com"] "123123" "Hi" "How you doing?")
          
          (db-assert/assert-datomic-temp-message-count 1)
          
          (is (= 1 (count (:user/temp-messages updated-u))))
          (is (= "How you doing?" (-> updated-u :user/temp-messages first :temp-message/text))))))))


(deftest test-distilled-temp-message
  (demonic-testing "when user sends a new message, distillation should work for temp messages"
    (let [vincent (vincent-persona/create)
          vincent-ui (-> vincent :user/user-identities first)
          vincent-uid (-> vincent-ui :identity/provider-uid str)
          vincent-guid (-> vincent :user/guid str)

          jill-ui (-> vincent :user/contacts first :contact/social-identities first)
          jill-uid (:social/provider-uid jill-ui)]
      (mocking [fb-chat/send-message]
        (m-service/new-message vincent {:text "Hey" :provider "facebook" :from vincent-uid :guid vincent-guid :to [jill-uid]}))
      (let [vincent (u-store/reload vincent)
            last-tm (->> vincent :user/temp-messages last)
            d-last-tm (->> last-tm (message/distill vincent))]

        (is (= (:temp-message/message-id last-tm) (:message/message-id d-last-tm)))
        (is (= (:temp-message/provider last-tm) (:message/provider d-last-tm)))
        (is (= (:temp-message/guid last-tm) (:message/guid d-last-tm)))
        (is (= vincent-uid (:message/from d-last-tm)))
        (same-value?  [jill-uid] (:message/to d-last-tm))
        
        (is (= (:identity/first-name vincent-ui) (get-in d-last-tm [:message/author :author/first-name])))
        (is (= (:identity/last-name vincent-ui) (get-in d-last-tm [:message/author :author/last-name])))
        (is (= (:identity/photo-url vincent-ui) (get-in d-last-tm [:message/author :author/picture-url])))

        (let [reply-to (-> d-last-tm :message/reply-to first)]
          (is (= (:social/first-name jill-ui) (get-in reply-to [:reply-to/first-name])))
          (is (= (:social/last-name jill-ui) (get-in reply-to [:reply-to/last-name])))
          (is (= (:social/provider-uid jill-ui) (get-in reply-to [:reply-to/provider-uid]))))

        (has-keys d-last-tm [:message/message-id :message/thread-id :message/snippet :message/sent :message/date :message/text])

        ))))