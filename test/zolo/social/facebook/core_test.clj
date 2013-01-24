(ns zolo.social.facebook.core-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.utils.debug
        zolo.scenarios.user
        conjure.core
        zolo.test.assertions)
  (:require [zolo.social.facebook.core :as fb-core]
            [zolo.social.core :as social]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.core :as lab]))

(defn request-params [fb-user]
  (-> fb-user
      lab/login-creds
      (assoc :provider "FACEBOOK")))

(defn fake-friends-list [at uid]
  (-> uid
      lab/get-user
      lab/fetch-friends))

(defn fake-extended-user-info [at uid]
  (-> uid
      lab/get-user
      lab/extended-user-info))

(deftest test-signup-user
  (lab/in-facebook-lab
   (stubbing [fb-gateway/extended-user-info fake-extended-user-info]
     (let [mickey (lab/create-user "Mickey" "Mouse")
           params (request-params mickey)
           cookies {}
           canonical-user (social/signup-user params cookies)]
       (assert-basic-user-info mickey canonical-user)
       (assert-user-identity mickey (first (:user/user-identities canonical-user)))))))


(deftest test-fetch-contacts
  (lab/in-facebook-lab
   (stubbing [fb-gateway/friends-list fake-friends-list]
     (let [mickey (lab/create-user "Mickey" "Mouse")
           donald (lab/create-friend "Donald" "Duck")
           daisy (lab/create-friend "Daisy" "Duck")]
       (lab/make-friend mickey donald)
       (lab/make-friend mickey daisy)

       (let [contacts (sort-by :contact/first-name (social/fetch-contacts :provider/facebook (:access-token mickey) (:uid mickey) nil))
             daisy-contact (first contacts)
             donald-contact (last contacts)]
         (is (= 2 (count contacts)))
         (assert-contact daisy daisy-contact)
         (assert-contact donald donald-contact))))))

