(ns zolo.wild.facebook-test
  (:use zolo.demonic.test
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.facebook.gateway :as fb-gateway]
            [zolo.marconi.facebook.factory :as fb-factory]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.loner :as loner]
            [zolo.personas.core :as personas]
            [zolo.test.assertions :as assertions]))

(demonictest test-birthday-is-nil
  (marconi/in-lab
   (let [u (personas/create-fb-user "User" "Main")
         c (-> (personas/create-fb-user "No" "Birthday")
               :id
               (fb/update-user {:birthday nil}))]
     
     
     (user/insert-fb-user u)

     (fb/make-friend u c)

     (personas/update-fb-friends u)

     (let [u-from-db (user/find-by-fb-id (:id u))]
       (is (= 1 (count (:user/contacts u-from-db))))
       (let [c-from-db (personas/friend-of u-from-db "No")]
         (is (not (nil? c-from-db)))
         ;;TODO This should be the real assertion
;;         (is (nil? (:contact/fb-birthday c-from-db)))
         (is (= #inst "1900-01-01T00:00:00.000-00:00" (:contact/fb-birthday c-from-db))))))))

(demonictest test-birthday-does-not-have-year
  (marconi/in-lab
   (let [u (personas/create-fb-user "User" "Main")
         c (-> (personas/create-fb-user "No" "Year")
               :id
               (fb/update-user {:birthday "05/22"}))]
     
     (user/insert-fb-user u)

     (fb/make-friend u c)

     (personas/update-fb-friends u)

     (let [u-from-db (user/find-by-fb-id (:id u))]
       (is (= 1 (count (:user/contacts u-from-db))))
       (let [c-from-db (personas/friend-of u-from-db "No")]
         (is (not (nil? c-from-db)))
         (is (= #inst "1900-05-22T00:00:00.000-00:00" (:contact/fb-birthday c-from-db))))))))


;; (demonictest test-message-attachments-are-map-not-string
;;   (is false "Need to implement this test"))

