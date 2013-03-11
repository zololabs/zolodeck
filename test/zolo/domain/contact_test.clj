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

;;TODO Duplicate Function
(defn create-social-user [fb-user]
  (-> fb-user
      (personas/request-params true)
      social/signup-user))

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


(deftest test-mute-contact
  (demonic-testing "Muting a contact"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           goofy (fb-lab/create-friend "Goofy" "Dog")
           db-mickey (user/signup-new-user (create-social-user mickey))]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey goofy)
       
       (fb-lab/login-as mickey)

       (contact/update-contacts (user/reload db-mickey))       
       
       (let [[db-donald db-goofy] (sort-by :contact/first-name (:user/contacts (user/reload db-mickey)))]
         (contact/set-muted db-goofy true)
         (d-assert/contact-is-muted (contact/reload db-goofy))
         (d-assert/contact-is-not-muted (contact/reload db-donald))))))

  (demonic-integration-testing "Muting, then updating"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           goofy (fb-lab/create-friend "Goofy" "Dog")
           db-mickey (in-demarcation (user/signup-new-user (create-social-user mickey)))]

       (fb-lab/make-friend mickey goofy)
       
       (fb-lab/login-as mickey)

       (in-demarcation (contact/update-contacts (user/reload db-mickey)))
       
       (in-demarcation
        (let [db-goofy (first (:user/contacts (user/reload db-mickey)))]
          (contact/set-muted db-goofy true)
          (d-assert/contact-is-muted (contact/reload db-goofy))))

       (fb-lab/make-friend mickey donald)
       (fb-lab/update-user (:id goofy) {:first_name "Giify"})       
       (in-demarcation (contact/update-contacts (user/reload db-mickey)))

       (in-demarcation (user/update-scores (user/reload db-mickey)))

       (in-demarcation
        (let [[db-donald db-giify] (sort-by :contact/first-name (:user/contacts (user/reload db-mickey)))]
          (d-assert/contact-is-muted (contact/reload db-giify))
          (is (= "Giify" (:social/first-name (first (:contact/social-identities db-giify)))))
          (d-assert/contact-is-not-muted (contact/reload db-donald))))))))

(deftest test-update-contacts-repeatedly
  (demonic-integration-testing  "Returning User"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           minnie (fb-lab/create-friend "Minnie" "Mouse")
           db-mickey (in-demarcation (user/signup-new-user (create-social-user mickey)))]

       (fb-lab/make-friend mickey donald)
       
       (fb-lab/login-as mickey)

       (in-demarcation
        (contact/update-contacts (user/reload db-mickey)))

       (in-demarcation
        (fb-lab/make-friend mickey minnie)
        (contact/update-contacts (user/reload db-mickey)))

       (in-demarcation
        (is (= 2 (count (versions db-mickey :user/contacts)))))

       (dotimes [n 100]
         (in-demarcation
          (contact/update-contacts (user/reload db-mickey))))
       
       (in-demarcation
        (is (= 2 (count (versions db-mickey :user/contacts)))))))))

(defn assert-all-contacts-are [strength-as-keyword contacts]
  (is (every? #(.contains (:contact/first-name %) (name strength-as-keyword))
              contacts)
      (str "Not all contacts are " strength-as-keyword)))

(defn assert-no-contacts-are [strength-as-keyword contacts]
  (is (not (some #(.contains (:contact/first-name %) (name strength-as-keyword))
                  contacts))
      (str "Unexpectedly found contact of strength " strength-as-keyword)))

(demonictest test-contact-list
  (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           db-mickey (user/signup-new-user (create-social-user mickey))
           strong-friends (fb-lab/create-friends "strong" 10)
           medium-friends (fb-lab/create-friends "medium" 20)
           weak-friends (fb-lab/create-friends "weak" 30)
           all-friends (concat strong-friends medium-friends weak-friends)]

       (doseq [f all-friends]
         (fb-lab/make-friend mickey f))
       
       (fb-lab/login-as mickey)
       
       (contact/update-contacts (user/reload db-mickey))

       (stubbing [contact/contact-score (fn [c]
                                          (let [fname (:contact/first-name c)]
                                            (cond 
                                             (.contains fname "strong") 500
                                             (.contains fname "medium") 200
                                             (.contains fname "weak") 50
                                             :else 0)))]
         
         (testing "No Selectors should return all contacts"
           (is (= 60 (count (contact/list-contacts (user/reload db-mickey) {})))))

         (testing "Selectors should return only Selected contacts"
           (is (= 10 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong]}))))
           (assert-all-contacts-are :strong (contact/list-contacts (user/reload db-mickey) {:selectors [:strong]}))
           
           (is (= 20 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:medium]}))))
           (assert-all-contacts-are :medium (contact/list-contacts (user/reload db-mickey) {:selectors [:medium]}))
           
           (is (= 30 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:weak]}))))
           (assert-all-contacts-are :weak (contact/list-contacts (user/reload db-mickey) {:selectors [:weak]}))
           
           (is (= 30 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :medium]}))))
           (assert-no-contacts-are :weak (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :medium]}))

           (is (= 40 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :weak]}))))
           (assert-no-contacts-are :medium (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :weak]}))

           (is (= 10 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :strong]})))))


         (testing "Pagingation"
           (is (= 5 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong] :limit 5}))))
           (is (= 10 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong] :limit 50}))))           
           (is (= 5 (count (contact/list-contacts (user/reload db-mickey) {:selectors [] :limit 50 :offset 55})))))))))