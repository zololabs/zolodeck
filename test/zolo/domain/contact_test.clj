(ns zolo.domain.contact-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.domain.contact :as contact]
            [zolo.setup.datomic-setup :as datomic-setup]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

(defn create-social-user [fb-user]
  (-> fb-user
      (personas/request-params true)
      (social/signup-user {})))

(deftest test-update-contacts
  (demonic-integration-testing  "Returning User"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           daisy (fb-lab/create-friend "Daisy" "Duck")
           minnie (fb-lab/create-friend "Minnie" "Mouse")
           db-mickey (in-demarcation (user/signup-new-user (create-social-user mickey)))]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey daisy)
       
       (fb-lab/login-as mickey)

       (in-demarcation
        (db-assert/assert-datomic-contact-count 0)
        (db-assert/assert-datomic-social-count 0))

       (in-demarcation
        (contact/update-contacts (user/reload db-mickey))
        (db-assert/assert-datomic-contact-count 2)
        (db-assert/assert-datomic-social-count 2))

       (in-demarcation
        (fb-lab/make-friend mickey minnie)
        (contact/update-contacts (user/reload db-mickey))
        
        (db-assert/assert-datomic-contact-count 3)
        (db-assert/assert-datomic-social-count 3))

       (let [[db-daisy db-donald db-minnie] (sort-by :contact/first-name (:user/contacts (in-demarcation (user/reload db-mickey))))]
         (d-assert/contacts-are-same daisy db-daisy)
         (d-assert/contacts-are-same donald db-donald)
         (d-assert/contacts-are-same minnie db-minnie))))))
