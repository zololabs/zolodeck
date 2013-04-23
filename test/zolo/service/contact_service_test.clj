(ns zolo.service.contact-service-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.clojure
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.message :as message]
            [zolo.domain.core :as d-core]
            [zolo.store.user-store :as u-store]
            [zolo.store.contact-store :as c-store]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.personas.generator :as pgen]))

(deftest test-update-contacts-for-user
  (demonic-testing "when user is not present, it should return nil"
    (is (nil? (c-service/update-contacts-for-user nil))))

  (demonic-testing  "User is present in the system and has NO contacts"
    (personas/in-social-lab
     (let [db-mickey (-> (fb-lab/create-user "Mickey" "Mouse")
                         personas/create-db-user-from-fb-user)]
       
       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (c-service/update-contacts-for-user db-mickey)
       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (is (= 0  (->> (c-service/update-contacts-for-user db-mickey)
                       :user/contacts
                       count))))))
  
  (demonic-testing  "User is present in the system and has contacts"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           daisy (fb-lab/create-friend "Daisy" "Duck")
           minnie (fb-lab/create-friend "Minnie" "Mouse")
           db-mickey (-> mickey
                         personas/create-db-user-from-fb-user)]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey daisy)       

       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (let [u-db-mickey (c-service/update-contacts-for-user db-mickey)]
         (db-assert/assert-datomic-contact-count 2)
         (db-assert/assert-datomic-social-count 2)
         
         (fb-lab/make-friend mickey minnie)
         
         (let [[db-daisy db-donald db-minnie] (->> (c-service/update-contacts-for-user u-db-mickey)
                                                   :user/contacts
                                                   (sort-by contact/first-name))]
           (db-assert/assert-datomic-contact-count 3)
           (db-assert/assert-datomic-social-count 3)
           
           (d-assert/contacts-are-same daisy db-daisy)
           (d-assert/contacts-are-same donald db-donald)
           (d-assert/contacts-are-same minnie db-minnie)))))))


(deftest test-update-scores
  (demonic-testing "Updating scores for all contacts"
    (personas/in-social-lab
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
                                  c-service/update-contacts-for-user
                                  m-service/update-inbox-messages
                                  c-service/update-scores)]

         (db-assert/assert-datomic-message-count 5)

         (let [[daisy donald minnie] (->> refreshed-mickey
                                          :user/contacts
                                          (sort-by contact/first-name))]

           (is (= 20 (:contact/score daisy)))
           (is (= 30 (:contact/score donald)))
           (is (= 0 (:contact/score minnie))))))))))

(demonictest test-get-contact-by-guid
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                             (pgen/create-friend-spec "Jill" "Ferry" 1 1)]}})
         [jack jill] (sort-by contact/first-name (:user/contacts u))]
    
     (testing "when user is not present it should return nil"
       (is (nil? (c-service/get-contact-by-guid nil (:contact/guid jack)))))

     (testing "when contact is not present it should return nil"
       (is (nil? (c-service/get-contact-by-guid u (random-guid-str)))))

     (testing "when user and contact is present it should return distilled contact"
       
       (let [distilled-jack (c-service/get-contact-by-guid u (:contact/guid jack))]
         (is (not (nil? distilled-jack)))
         (is (= (:contact/guid jack) (:contact/guid distilled-jack))))))))

(demonictest test-update-contact
  (d-core/run-in-gmt-tz
   (run-as-of "2012-05-11"
     (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
           ibc (interaction/ibc u)
           jack (first (:user/contacts u))]

       (testing "When user is not present it should return nil"
         (is (nil? (c-service/update-contact nil jack {:muted true}))))
       
       (testing "When contact is not present it should return nil"
         (is (nil? (c-service/update-contact u nil {:muted true}))))
       
       (testing "When invalid contact is send it should throw exception"
         (is (thrown-with-msg? RuntimeException #"bad-request"
               (c-service/update-contact u jack {:muted ""})))
         (is (thrown-with-msg? RuntimeException #"bad-request"
               (c-service/update-contact u jack {:text "Hey"}))))
       
       (testing "When called with proper attributes it should update contact"
         (db-assert/assert-datomic-contact-count 1)
         (db-assert/assert-datomic-social-count 1)

         (is (not (contact/is-muted? jack)))
         
         (let [updated-jack (c-service/update-contact u jack {:muted true})]

           (db-assert/assert-datomic-contact-count 1)
           (db-assert/assert-datomic-social-count 1)
           
           (is (contact/is-muted? updated-jack))
           (is (contact/is-muted? (c-store/find-by-guid (:contact/guid jack))))

           (is (= (dissoc (contact/distill jack ibc) :contact/muted)
                  (dissoc updated-jack :contact/muted)))))))))



