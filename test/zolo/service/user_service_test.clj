(ns zolo.service.user-service-test
  (:use [clojure.test :only [deftest is are testing]]
        zolo.utils.debug
        zolo.utils.clojure
        zolo.test.core-utils
        zolo.test.assertions.core
        zolo.demonic.test)
  (require [zolo.service.user-service :as u-service]
           [zolo.domain.user-identity :as user-identity]
           [zolo.domain.social-identity :as si]
           [zolo.store.user-store :as u-store]
           [zolo.personas.factory :as personas]
           [zolo.domain.contact :as contact]
           [zolo.domain.message :as message]
           [zolo.test.assertions.datomic :as db-assert]
           [zolo.test.assertions.domain :as d-assert]
           [zolo.marconi.facebook.core :as fb-lab]
           [zolo.marconi.context-io.core :as e-lab]
           [zolo.personas.generator :as pgen]))

(deftest test-get-users
  (demonic-testing "when user is not present it should return nil"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]
       (is (nil? (u-service/get-users (personas/fb-request-params mickey true)))))))

  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey1 (u-service/new-user (personas/fb-request-params mickey true))
           d-mickey2 (u-service/get-users (personas/fb-request-params mickey true))]

       (is (= d-mickey1 d-mickey2))))))

(deftest test-get-user-by-guid
  (demonic-testing "when user is not present it should return nil"
    (personas/in-social-lab
     (is (nil? (u-service/get-user-by-guid (random-guid-str))))))

  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey1 (u-service/new-user (personas/fb-request-params mickey true))
           d-mickey2 (u-service/get-user-by-guid (:user/guid d-mickey1))]

       (is (= d-mickey1 d-mickey2))))))

(deftest test-update-user
  (demonic-testing "when user is not present, it should return nil"
    (is (nil? (u-service/update-user (zolo.utils.clojure/random-guid-str) {}))))

  (demonic-testing "when user is present, it should return updated distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]

       (fb-lab/login-as mickey)
       
       (let [mickey-guid (:user/guid (u-service/new-user (personas/fb-request-params mickey false 420)))
             db-mickey-1 (u-store/find-by-guid mickey-guid)
             _ (u-service/update-user mickey-guid (personas/fb-request-params mickey true 800))
             db-mickey-2 (u-store/find-by-guid mickey-guid)]

         (is (not (user-identity/fb-permissions-granted? db-mickey-1)))
         (is (user-identity/fb-permissions-granted? db-mickey-2))

         (is (= 420 (:user/login-tz db-mickey-1)))
         (is (= 800 (:user/login-tz db-mickey-2)))

         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1))))))

(deftest test-new-user-using-facebook
  (demonic-testing "new user sign up using Facebook - good request"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]

       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-user-count 0)
       (db-assert/assert-datomic-user-identity-count 0)
       
       (let [distilled-mickey (u-service/new-user (personas/fb-request-params mickey true))
             d-mickey (u-store/reload distilled-mickey)]
         (is (= ["Mickey.Mouse@gmail.com"] (:user/emails distilled-mickey)))
         (is (:user/data-ready-in distilled-mickey))
         (is (>= (:user/data-ready-in distilled-mickey) (* 90 60)))

         (is (-> d-mickey user-identity/fb-user-identity :identity/permissions-granted))
         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1)))))

  (demonic-testing "new user sign up - bad request"

    (thrown+? {:type :bad-request :error ["[:access_token] is not string"
                                          "[:access_token] is required"
                                          "[:login_provider_uid] is not string"
                                          "[:login_provider_uid] is required"
                                          "[:login_tz] is not integer"
                                          "[:login_tz] is required"]}
              (u-service/new-user (personas/fb-request-params {} true nil)))

    (thrown+? {:type :bad-request :error ["[:access_token] is not string"
                                          "[:access_token] is required"
                                          "[:login_provider_uid] is not string"
                                          "[:login_provider_uid] is required"
                                          "[:login_tz] is not integer"]}
                  (u-service/new-user (personas/fb-request-params {} true "JUNK-TIME-TZ")))

    (db-assert/assert-datomic-user-count 0)
    (db-assert/assert-datomic-user-identity-count 0)))


(deftest test-new-user-using-email
  (demonic-testing "new user sign up using Google - good request"
    (personas/in-social-lab
     (let [mickey (e-lab/create-account "Mickey" "Mouse" "Mickey.Mouse@gmail.com")]

       (db-assert/assert-datomic-user-count 0)
       (db-assert/assert-datomic-user-identity-count 0)
       
       (let [distilled-mickey (u-service/new-user (personas/email-request-params mickey true))
             d-mickey (u-store/reload distilled-mickey)]
         (is (= ["Mickey.Mouse@gmail.com"] (:user/emails distilled-mickey)))
         (is (:user/data-ready-in distilled-mickey))
         (is (>= (:user/data-ready-in distilled-mickey) (* 90 60)))         

         (is (-> d-mickey :user/user-identities first :identity/permissions-granted))
         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1))))))


(demonictest ^:storm test-refresh-user-data-and-scores-for-facebook
  (personas/in-fb-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-friend "Donald" "Duck")
         daisy (fb-lab/create-friend "Daisy" "Duck")
         minnie (fb-lab/create-friend "Minnie" "Mouse")
         db-mickey (personas/create-db-user-from-fb-user mickey)]
     
     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)
     (fb-lab/make-friend mickey minnie)

     (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01 00:00")
           m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02 00:00")
           m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03 00:00")
           m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01 00:00")
           m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02 00:00")]
       
       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-message-count 0)

       (let [refreshed-mickey (-> db-mickey
                                  u-service/refresh-user-data
                                  u-service/refresh-user-scores)]

         (testing "should stamp refresh start time"
           (is (not (nil? (:user/refresh-started refreshed-mickey)))))

         (testing "should extend fb token"
           (is (not (nil? (user-identity/fb-access-token refreshed-mickey))))
           (is (not (= (user-identity/fb-access-token db-mickey)
                       (user-identity/fb-access-token refreshed-mickey)))))

         (testing "should update contacts"

           (let [[db-daisy db-donald db-minnie] (->> refreshed-mickey
                                                     :user/contacts
                                                     (sort-by contact/first-name))]

             (db-assert/assert-datomic-contact-count 3)
             (db-assert/assert-datomic-social-count 3)

             (d-assert/contacts-are-same daisy db-daisy)
             (d-assert/contacts-are-same donald db-donald)
             (d-assert/contacts-are-same minnie db-minnie)))

         (testing "should update messages"
           (db-assert/assert-datomic-message-count 5)

           (d-assert/messages-list-are-same [m1 m2 m3 m4 m5] (->> refreshed-mickey
                                                                  :user/messages
                                                                  (sort-by message/message-date))))

         (testing "should update scores"
           (let [[db-daisy db-donald db-minnie] (->> refreshed-mickey
                                                     :user/contacts
                                                     (sort-by contact/first-name))]
             
             (is (= 20 (:contact/score db-daisy)))
             (is (= 30 (:contact/score db-donald)))
             (is (= 0 (:contact/score db-minnie)))))

         (testing "should stamp last updated date"
           (is (not (nil? (:user/last-updated refreshed-mickey))))))))))


(demonictest ^:storm test-refresh-user-data-and-scores-for-email
  (run-as-of "2012-05-07"
    (personas/in-email-lab
     (let [mickey-email "mickey@gmail.com"
           mickey (e-lab/create-account "Mickey" "Mouse" mickey-email)
           db-mickey (personas/create-db-user-from-email-user mickey)]
       
       (let [m1 (e-lab/send-message mickey-email "donald@gmail.com" "s1" "t1" "Hi, what's going on?" "2012-05-01 00:00")
             m2 (e-lab/send-message "donald@gmail.com" mickey-email "s2" "t2" "Nothing, just work." "2012-05-02 00:00")
             m3 (e-lab/send-message mickey-email "donald@gmail.com" "s3" "t3" "OK, groceries?" "2012-05-03 00:00")

             m4 (e-lab/send-message "daisy@gmail.com" mickey-email "s4" "t4" "Nothing, just work." "2012-05-04 00:00")
             m5 (e-lab/send-message mickey-email "daisy@gmail.com" "s5" "t4" "OK, groceries?" "2012-05-05 00:00")

             m6 (e-lab/send-message mickey-email "admin@thoughtworks.com" "s6" "t6" "Special Deal!" "2012-05-05 00:00")]
         
         (db-assert/assert-datomic-message-count 0)

         (let [refreshed-mickey (pgen/refresh-everything db-mickey)]

           (testing "should stamp refresh start time"
             (is (not (nil? (:user/refresh-started refreshed-mickey)))))

           (testing "should update contacts"

             (let [[db-tw db-daisy db-donald] (->> refreshed-mickey
                                                   :user/contacts
                                                   (sort-by contact/first-name))]

               (db-assert/assert-datomic-contact-count 3)
               (db-assert/assert-datomic-social-count 3)

               (is (= "admin@thoughtworks.com" (-> db-tw :contact/social-identities first :social/provider-uid)))
               (is (= "admin@thoughtworks.com" (-> db-tw :contact/social-identities first :social/email)))

               (is (= "daisy@gmail.com" (-> db-daisy :contact/social-identities first :social/provider-uid)))
               (is (= "daisy@gmail.com" (-> db-daisy :contact/social-identities first :social/email)))

               (is (= "donald@gmail.com" (-> db-donald :contact/social-identities first :social/provider-uid)))
               (is (= "donald@gmail.com" (-> db-donald :contact/social-identities first :social/email)))))

           (testing "should update messages"
             (db-assert/assert-datomic-message-count 6)

             (d-assert/messages-list-are-same [m1 m2 m3 m4 m5 m6] (->> refreshed-mickey
                                                                       :user/messages
                                                                       (sort-by message/message-date))))

           (testing "should know about person/not-a-person"
             (let [[db-tw db-daisy db-donald] (->> refreshed-mickey
                                                   :user/contacts
                                                   (sort-by contact/first-name))]

               (is (-> db-daisy  :contact/social-identities first si/is-a-person?))
               (is (-> db-donald :contact/social-identities first si/is-a-person?))
               (is (not (-> db-tw :contact/social-identities first si/is-a-person?)))))
           
           (testing "should update scores"
             (let [[db-tw db-daisy db-donald] (->> refreshed-mickey
                                                   :user/contacts
                                                   (sort-by contact/first-name))]

               (is (= 10 (:contact/score db-tw)))
               (is (= 20 (:contact/score db-daisy)))
               (is (= 30 (:contact/score db-donald)))))

           (testing "should stamp last updated date"
             (is (not (nil? (:user/last-updated refreshed-mickey)))))))))))

(demonictest ^:storm test-refresh-user-data-and-scores-using-gen
  (personas/in-social-lab
   (run-as-of "2012-05-12"
     (pgen/run-generative-tests
         u {:SPECS {:friends [(pgen/create-friend-spec "Donald" "Duck" 3 5)
                              (pgen/create-friend-spec "Daisy" "Duck" 2 4)
                              (pgen/create-friend-spec "Minnie" "Mouse" 1 1)]}
            :UI-IDS-ALLOWED [:FACEBOOK :EMAIL]
            :UI-IDS-COUNT 1}
         
         (testing "should stamp refresh start time"
           (is (not (nil? (:user/refresh-started u)))))
         
         (testing "should update contacts"
           
           (let [[db-daisy db-donald db-minnie] (->> u
                                                     :user/contacts
                                                     (sort-by contact/first-name))]
             
             (is (= 3 (count (:user/contacts u))))))
         
         (testing "should update messages"
           (is (= 10 (count (:user/messages u)))))
         
         (testing "should update scores"
           (let [[db-daisy db-donald db-minnie] (->> u
                                                     :user/contacts
                                                     (sort-by contact/first-name))]
             
             (is (= 20 (:contact/score db-daisy)))
             (is (= 30 (:contact/score db-donald)))
             (is (= 10 (:contact/score db-minnie)))))
         
         
         (testing "should stamp last updated date"
           (is (not (nil? (:user/last-updated u)))))))))