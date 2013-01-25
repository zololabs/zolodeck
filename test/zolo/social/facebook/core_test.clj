(ns zolo.social.facebook.core-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.utils.debug
        zolo.scenarios.user
        conjure.core)
  (:require [zolo.social.facebook.core :as fb-core]
            [zolo.social.core :as social]
            [zolo.domain.message :as message]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.social.facebook.messages :as fb-messages]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]
            [zolo.test.assertions.canonical :as c-assert]
            [zolo.personas.factory :as personas]))

(deftest test-signup-user
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         params (personas/request-params mickey true)
         cookies {}
         canonical-user (social/signup-user params cookies)]
     (c-assert/assert-basic-user-info mickey canonical-user)
     (c-assert/assert-user-identity mickey (first (:user/user-identities canonical-user))))))


(deftest test-fetch-contacts
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-friend "Donald" "Duck")
         daisy (fb-lab/create-friend "Daisy" "Duck")]
     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (let [contacts (sort-by :contact/first-name (social/fetch-contacts :provider/facebook (:access-token mickey) (:uid mickey) nil))
           daisy-contact (first contacts)
           donald-contact (last contacts)]
       (is (= 2 (count contacts)))
       (c-assert/assert-contact daisy daisy-contact)
       (c-assert/assert-contact donald donald-contact)))))


(deftest test-fetch-messages
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-user "Donald" "Duck")
         daisy (fb-lab/create-user "Daisy" "Duck")]

     (fb-lab/login-as mickey)

     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
           m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
           m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
           
           m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
           m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
       
       (let [fb-messages (sort-by :message/date (social/fetch-messages :provider/facebook (:access-token mickey) (:uid mickey) message/MESSAGES-START-TIME-SECONDS))]
         (c-assert/assert-message m1 (nth fb-messages 0))
         (c-assert/assert-message m2 (nth fb-messages 1))
         (c-assert/assert-message m3 (nth fb-messages 2))
         (c-assert/assert-message m4 (nth fb-messages 3))
         (c-assert/assert-message m5 (nth fb-messages 4)))))))

