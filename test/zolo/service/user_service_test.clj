(ns zolo.service.user-service-test
  (:use [clojure.test :only [deftest is are testing]]
        zolodeck.utils.debug
        zolodeck.utils.clojure
        zolo.test.assertions.core
        zolodeck.demonic.test)
  (require [zolo.service.user-service :as u-service]
           [zolo.domain.user-identity :as user-identity]
           [zolo.store.user-store :as u-store]
           [zolo.personas.factory :as personas]
           [zolo.domain.contact :as contact]
           [zolo.domain.message :as message]
           [zolo.test.assertions.datomic :as db-assert]
           [zolo.test.assertions.domain :as d-assert]
           [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

(deftest test-get-users
  (demonic-testing "when user is not present it should return nil"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]
       (is (nil? (u-service/get-users (personas/request-params mickey true)))))))

  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey1 (u-service/new-user (personas/request-params mickey true))
           d-mickey2 (u-service/get-users (personas/request-params mickey true))]

       (is (= d-mickey1 d-mickey2))))))

(deftest test-get-user-by-guid
  (demonic-testing "when user is not present it should return nil"
    (personas/in-social-lab
     (is (nil? (u-service/get-user-by-guid (random-guid-str))))))

  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey1 (u-service/new-user (personas/request-params mickey true))
           d-mickey2 (u-service/get-user-by-guid (:user/guid d-mickey1))]

       (is (= d-mickey1 d-mickey2))))))

(deftest test-update-user
  (demonic-testing "when user is not present, it should return nil"
    (is (nil? (u-service/update-user (zolodeck.utils.clojure/random-guid-str) {}))))

  (demonic-testing "when user is present, it should return updated distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]

       (fb-lab/login-as mickey)
       
       (let [mickey-guid (:user/guid (u-service/new-user (personas/request-params mickey false)))
             db-mickey-1 (u-store/find-by-guid mickey-guid)
             _ (u-service/update-user mickey-guid (personas/request-params mickey true))
             db-mickey-2 (u-store/find-by-guid mickey-guid)]

         (is (not (user-identity/fb-permissions-granted? db-mickey-1)))
         (is (user-identity/fb-permissions-granted? db-mickey-2))

         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1))))))


(deftest test-new-user
  (demonic-testing "new user sign up - good request"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")]

       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-user-count 0)
       (db-assert/assert-datomic-user-identity-count 0)
       
       (let [distilled-mickey (u-service/new-user (personas/request-params mickey true))]
         (is (= "Mickey.Mouse@gmail.com" (:user/email distilled-mickey)))

         (db-assert/assert-datomic-user-count 1)
         (db-assert/assert-datomic-user-identity-count 1)))))

  (demonic-testing "new user sign up - bad request"

    (thrown+? {:type :bad-request :error ["[:access_token] is required"
                                          "[:login_provider_uid] is required"]}
              (u-service/new-user (personas/request-params {} true)))

    (db-assert/assert-datomic-user-count 0)
    (db-assert/assert-datomic-user-identity-count 0)))


(demonictest ^:storm test-refresh-user-data-and-scores
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-friend "Donald" "Duck")
         daisy (fb-lab/create-friend "Daisy" "Duck")
         minnie (fb-lab/create-friend "Minnie" "Mouse")
         db-mickey (personas/create-db-user mickey)]
     
     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)
     (fb-lab/make-friend mickey minnie)

     (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
           m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
           m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
           m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
           m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
       
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