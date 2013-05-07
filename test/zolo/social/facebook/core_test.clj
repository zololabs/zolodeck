(ns zolo.social.facebook.core-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        zolo.scenarios.user
        conjure.core)
  (:require [zolo.social.facebook.core :as fb-core]
            [zolo.social.core :as social]
            [zolo.domain.message :as message]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.social.facebook.messages :as fb-messages]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.test.assertions.canonical :as c-assert]
            [zolo.personas.factory :as personas]
            [zolo.personas.generator :as pgen]))

(def DUMMY-MESSAGES-START-TIME-SECONDS 123123123)

(deftest test-signup-user
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         params (personas/fb-request-params mickey true)
         canonical-user (social/fetch-user-identity params)]
     (c-assert/assert-user-identity mickey canonical-user))))


(deftest test-fetch-contacts
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-friend "Donald" "Duck")
         daisy (fb-lab/create-friend "Daisy" "Duck")]
     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (let [[daisy-contact donald-contact] (sort-by :contact/first-name (social/fetch-social-identities :provider/facebook
                                                                                 (:access-token mickey)
                                                                                 (:uid mickey) nil))]

       (c-assert/assert-social-identity daisy daisy-contact)
       (c-assert/assert-social-identity donald donald-contact)))))


(deftest test-fetch-messages
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-user "Donald" "Duck")
         daisy (fb-lab/create-user "Daisy" "Duck")]

     (fb-lab/login-as mickey)

     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01 00:00")
           m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02 00:00")
           m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03 00:00")
           
           m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01 00:00")
           m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02 00:00")]
       
       (let [fb-messages (sort-by message/message-date (social/fetch-messages :provider/facebook (:access-token mickey) (:uid mickey) DUMMY-MESSAGES-START-TIME-SECONDS))]
         (c-assert/assert-message m1 (nth fb-messages 0))
         (c-assert/assert-message m2 (nth fb-messages 1))
         (c-assert/assert-message m3 (nth fb-messages 2))
         (c-assert/assert-message m4 (nth fb-messages 3))
         (c-assert/assert-message m5 (nth fb-messages 4)))))))


(deftest test-fetch-feeds
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-user "Donald" "Duck")
         daisy (fb-lab/create-user "Daisy" "Duck")]
     
     (fb-lab/login-as mickey)
     
     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (let [f1 (fb-lab/post-to-wall mickey donald "hey check this link out" "2012-05-01")
           f2 (fb-lab/post-to-wall mickey daisy "happy birthday" "2012-05-02")
           f3 (fb-lab/post-to-wall daisy donald "check this picture" "2012-06-02")]
       
       (let [[donald1 donald3] (sort-by message/message-date (social/fetch-feed :provider/facebook (:access-token mickey) (:uid donald) DUMMY-MESSAGES-START-TIME-SECONDS))]
         (c-assert/assert-feed f1 donald1)
         (c-assert/assert-feed f3 donald3))))))

