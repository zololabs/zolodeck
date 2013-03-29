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
            [zolo.service.message-service :as m-service]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.message :as message]
            [zolo.store.user-store :as u-store]
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


;; (deftest test-update-scores
;;   (demonic-testing "Updating scores for all contacts"
;;     (personas/in-social-lab
;;    (let [mickey (fb-lab/create-user "Mickey" "Mouse")
;;          donald (fb-lab/create-friend "Donald" "Duck")
;;          daisy (fb-lab/create-friend "Daisy" "Duck")
;;          minnie (fb-lab/create-friend "Minnie" "Mouse")
;;          db-mickey (personas/create-db-user mickey)]

;;      (fb-lab/make-friend mickey donald)
;;      (fb-lab/make-friend mickey daisy)
;;      (fb-lab/make-friend mickey minnie)

;;      (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
;;            m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
;;            m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
;;            m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
;;            m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
       
;;        (fb-lab/login-as mickey)

;;        (db-assert/assert-datomic-message-count 0)

;;        (let [refreshed-mickey (-> db-mickey
;;                                   :user/guid
;;                                   c-service/update-contacts-for-user
;;                                   :user/guid
;;                                   m-service/update-inbox-messages
;;                                   :user/guid
;;                                   c-service/update-scores)]

;;          (db-assert/assert-datomic-message-count 5)

;;          (let [[daisy donald minnie] (->> refreshed-mickey
;;                                           :user/contacts
;;                                           (sort-by contact/first-name))]

;;            (is (= 20 (:contact/score daisy)))
;;            (is (= 30 (:contact/score donald)))
;;            (is (= 0 (:contact/score minnie))))))))))


