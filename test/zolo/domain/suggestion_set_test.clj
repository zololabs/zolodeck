(ns zolo.domain.suggestion-set-test
  (:use zolodeck.demonic.test
        zolodeck.demonic.core
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.suggestion-set :as ss]
            [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

(deftest test-suggestion-set
  (demonic-integration-testing "Should create new suggestion set"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           daisy (fb-lab/create-friend "Daisy" "Duck")
           minnie (fb-lab/create-friend "Minnie" "Mouse")
           db-mickey (in-demarcation (user/signup-new-user (personas/create-social-user mickey)))]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey daisy)
       (fb-lab/make-friend mickey minnie)
       
       (fb-lab/login-as mickey)

       (in-demarcation
        (contact/update-contacts (user/reload db-mickey)))

       (let [[db-daisy db-donald db-minnie] (in-demarcation
                                             (sort-by :contact/first-name (:user/contacts (in-demarcation (user/reload db-mickey)))))]
         

         (in-demarcation
          (stubbing [ss/suggestion-set-contacts [db-daisy]]
            (ss/new-suggestion-set (user/reload db-mickey) "ss-2012-05-01")))
         
         (in-demarcation
          (let [suggestion-set (ss/suggestion-set (user/reload db-mickey) "ss-2012-05-01")
                suggested-contacts (:suggestion-set/contacts suggestion-set)]
            (is (not (nil? suggestion-set)))
            (is (= "ss-2012-05-01" (:suggestion-set/name suggestion-set)))
            (is (= 1 (count suggested-contacts)) "Suggested only one contact ... so suggestion set should be 1")
            (d-assert/contacts-are-same daisy (first suggested-contacts))))

         (in-demarcation
          (is (nil? (user/suggestion-set (user/reload db-mickey) "2012-05-02"))  "Suggestion Set should be nil")))))))