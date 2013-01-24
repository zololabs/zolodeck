(ns zolo.domain.contact-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolo.test.core-utils
        zolo.test.assertions
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.social.core :as social]
            [zolo.domain.contact :as contact]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

(defn create-social-user [fb-user]
  (-> fb-user
      (personas/request-params true)
      (social/signup-user {})))

(deftest test-update-contacts
  (demonic-testing "First time User"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           daisy (fb-lab/create-friend "Daisy" "Duck")]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey daisy)
       
       (fb-lab/login-as mickey)

       (let [db-mickey (user/signup-new-user (create-social-user mickey))]
         (assert-datomic-contact-count 0)
         (assert-datomic-social-count 0)

         (contact/update-contacts (user/reload db-mickey))

         (assert-datomic-contact-count 2)
         (assert-datomic-social-count 2)

         (fb-lab/make-friend mickey (fb-lab/create-friend "Minnie" "Mouse"))

         (contact/update-contacts (user/reload db-mickey))

         (assert-datomic-contact-count 3)
         (assert-datomic-social-count 3))))))
