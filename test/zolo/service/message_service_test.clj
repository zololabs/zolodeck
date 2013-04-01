(ns zolo.service.message-service-test
  (:use zolodeck.utils.debug
        clojure.test
        zolodeck.demonic.test)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.domain.message :as message]
            [zolo.store.user-store :as u-store]
            [zolo.store.message-store :as m-store]
            [zolo.marconi.facebook.core :as fb-lab]))

(demonictest test-update-inbox-messages
  (testing "when user is not present, it should return nil"
    (is (nil? (m-service/update-inbox-messages nil))))
    
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-friend "Donald" "Duck")
         daisy (fb-lab/create-friend "Daisy" "Duck")
         db-mickey (personas/create-db-user mickey)]

     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
           m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
           m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")]
       
       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-message-count 0)

       (testing  "User with no previous messages"
         (let [refreshed-mickey (-> db-mickey
                                    c-service/update-contacts-for-user
                                    m-service/update-inbox-messages)]

           (db-assert/assert-datomic-message-count 3)

           (d-assert/messages-list-are-same [m1 m2 m3] (->> refreshed-mickey
                                                            :user/messages
                                                            (sort-by message/message-date)))))

       (testing "When previous messages are present it should not override"

           (fb-lab/remove-all-messages mickey)
           
           (let [m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
                 m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
         
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
           db-mickey (personas/create-db-user mickey)]
       
       (fb-lab/make-friend mickey donald)
       
       (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
             tm1 (personas/create-temp-message db-mickey "to-uid1" "Hello")
             u-db-mickey (m-store/append-temp-message db-mickey tm1)]
       
       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-temp-message-count 1)

       (testing  "User with no previous messages"
         (let [refreshed-mickey (-> u-db-mickey
                                    c-service/update-contacts-for-user
                                    m-service/update-inbox-messages)]

           (db-assert/assert-datomic-temp-message-count 0))))))))
