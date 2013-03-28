(ns zolo.service.contact-service-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.contact-service :as c-service]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.message :as message]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]
            [zolodeck.utils.calendar :as zolo-cal]))

(deftest test-update-contacts-for-user
  (demonic-testing "when user is not present, it should return nil"
    (is (nil? (c-service/update-contacts-for-user (zolodeck.utils.clojure/random-guid-str)))))

  (demonic-testing  "User is present in the system and has NO contacts"
    (personas/in-social-lab
     (let [db-mickey-key (-> (fb-lab/create-user "Mickey" "Mouse")
                             personas/create-db-user
                             :user/guid)]
       
       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (c-service/update-contacts-for-user db-mickey-key)
       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (is (= 0  (->> (c-service/update-contacts-for-user db-mickey-key)
                       :user/contacts
                       count))))))
  
  (demonic-testing  "User is present in the system and has contacts"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           daisy (fb-lab/create-friend "Daisy" "Duck")
           minnie (fb-lab/create-friend "Minnie" "Mouse")
           db-mickey-key (-> mickey
                             personas/create-db-user
                             :user/guid)]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey daisy)       

       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (c-service/update-contacts-for-user db-mickey-key)
       (db-assert/assert-datomic-contact-count 2)
       (db-assert/assert-datomic-social-count 2)
       
       (fb-lab/make-friend mickey minnie)
       
       (let [[db-daisy db-donald db-minnie] (->> (c-service/update-contacts-for-user db-mickey-key)
                                                 :user/contacts
                                                 (sort-by contact/first-name))]
         (db-assert/assert-datomic-contact-count 3)
         (db-assert/assert-datomic-social-count 3)
         
         (d-assert/contacts-are-same daisy db-daisy)
         (d-assert/contacts-are-same donald db-donald)
         (d-assert/contacts-are-same minnie db-minnie))))))


