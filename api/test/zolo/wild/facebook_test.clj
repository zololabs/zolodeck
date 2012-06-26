(ns zolo.wild.facebook-test
  (:use zolodeck.demonic.test
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.loner :as loner]
            [zolo.personas.core :as personas]
            [zolo.test.assertions :as assertions]))

(demonictest test-birthday-is-nil
  (fb/in-facebook-lab
   (let [u (personas/create-fb-user "User" "Main")
         c (print-vals "Contact from FB" (-> (personas/create-fb-user "No" "Birthday")
                                             (assoc :birthday nil)))]
     (user/insert-fb-user u)

     (fb/make-friend u c)

     (personas/update-fb-friends u)

     (let [u-from-db (user/find-by-fb-id (:id u))]
       (is (= 1 (count (:user/contacts u-from-db))))
       (let [c-from-db (print-vals "Contact " (personas/friend-of u-from-db "No"))]
         (is (not (nil? c-from-db)))
         (is (nil? (:contact/fb-birthday c-from-db))))))))

(comment
  (demonictest test-birthday-does-not-have-year 
    '(let [vincent (vincent/create)
           jack (personas/friend-of vincent "jack")
           c {:contact/fb-id "10000"
              :contact/first-name "Jack2"
              :contact/fb-birthday "05/22"}]
       (contact/create-contact vincent c)
       (is (= 3 (count (:user/contacts (user/reload vincent)))))
       (let [jack2 (personas/friend-of (user/reload vincent) "jack2")]
         (is (not (nil? jack2)))
         (is (not (nil? (:contact/fb-birthday jack2))))))))

