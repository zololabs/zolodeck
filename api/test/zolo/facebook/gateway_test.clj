(ns zolo.facebook.gateway-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.scenario
        zolo.test.core-utils
        zolo.test.web-utils
        zolo.utils.debug
        zolo.web.status-codes
        zolodeck.clj-social-lab.facebook)
  (:require [zolodeck.clj-social-lab.facebook.user :as test-user]
            [zolo.facebook.gateway :as gateway]
            [zolo.api.user-api :as user-api]))

(integration-test test-friends-list
  (in-facebook-lab 
   gateway/APP-ID gateway/APP-SECRET
   (let [jack (test-user/create "jack")
         jill (test-user/create "jill")
         mary (test-user/create "mary")]
     (login-as jack)
     (test-user/make-friend jill)
     (test-user/make-friend mary)

     (let [friends-of-jack (gateway/friends-list (test-user/access-token))]
       (is (= 2 (count friends-of-jack)))
       ;;TODO We need to verify the return values
       (is (some #(= "jill" (:name %)) friends-of-jack))
       (is (some #(= "mary" (:name %)) friends-of-jack))))))



