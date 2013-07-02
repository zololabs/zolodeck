(ns zolo.store.message-store-test
  (:use clojure.test
        zolo.utils.debug
        zolo.demonic.test)
  (:require [zolo.store.message-store :as m-store]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.personas.factory :as personas]
            [zolo.domain.message :as message]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.social.core :as social]))

(def DUMMY-MESSAGES-START-TIME-SECONDS 123123123)

(demonictest test-append-messages
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

       (let [messages (social/fetch-messages :provider/facebook "at" "pid" DUMMY-MESSAGES-START-TIME-SECONDS)
             u-db-mickey (m-store/append-messages db-mickey messages)]

         (testing "When no messages are present"
           (db-assert/assert-datomic-message-count 3)

           (d-assert/messages-list-are-same [m1 m2 m3] (->> u-db-mickey
                                                            :user/messages
                                                            (sort-by message/message-date))))

         (testing "When previous messages are present it should not override"

           (fb-lab/remove-all-messages mickey)
           
           (let [m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01 00:00")
                 m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02 00:00")]
         
             (db-assert/assert-datomic-message-count 3)

             (let [messages (social/fetch-messages :provider/facebook "at" "pid" DUMMY-MESSAGES-START-TIME-SECONDS)
                   u-db-mickey (m-store/append-messages u-db-mickey messages)]

               (db-assert/assert-datomic-message-count 5)
               (d-assert/messages-list-are-same [m1 m2 m3 m4 m5] (->> u-db-mickey
                                                                      :user/messages
                                                                      (sort-by message/message-date)))))))))))


(demonictest test-append-temp-message
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         db-mickey (personas/create-db-user-from-fb-user mickey)
         db-mickey-ui (-> db-mickey :user/user-identities first)]

     (db-assert/assert-datomic-temp-message-count 0)

     (let [tm1 (personas/create-temp-message db-mickey db-mickey-ui "to-uid1" "Hello")
           u-db-mickey (m-store/append-temp-message db-mickey tm1)]

       (db-assert/assert-datomic-temp-message-count 1)
       (d-assert/temp-messages-are-same tm1 (-> u-db-mickey :user/temp-messages first))

       (let [tm2 (personas/create-temp-message db-mickey db-mickey-ui "to-uid2" "How are you?")
             u-db-mickey (m-store/append-temp-message db-mickey tm2)]

         (db-assert/assert-datomic-temp-message-count 2))))))

(demonictest test-delete-temp-message
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         db-mickey (personas/create-db-user-from-fb-user mickey)
         db-mickey-ui (-> db-mickey :user/user-identities first)]

     (db-assert/assert-datomic-temp-message-count 0)

     (let [tm1 (personas/create-temp-message db-mickey db-mickey-ui "to-uid1" "Hello")
           u-db-mickey (m-store/append-temp-message db-mickey tm1)]

       (is (not (nil? (:user/temp-messages u-db-mickey))))
       (db-assert/assert-datomic-temp-message-count 1)

       (let [u-db-mickey (m-store/delete-temp-messages u-db-mickey)]
         (is (nil? (:user/temp-messages u-db-mickey))))

       (db-assert/assert-datomic-temp-message-count 0)))))





