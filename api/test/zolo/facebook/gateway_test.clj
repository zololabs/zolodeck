(ns zolo.facebook.gateway-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.scenario
        zolo.test.core-utils
        zolo.test.web-utils
        zolodeck.utils.debug
        zolo.web.status-codes)
  (:require [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.facebook.gateway :as gateway]
            [zolo.api.user-api :as user-api]))

;; (deftest ^:integration test-me
;;   (in-facebook-lab 
;;    (let [jack (test-user/create "jack")]
;;      (login-as jack)

;;      (let [jack-details (gateway/me (test-user/access-token))]
;;        ;;TODO Better way to test these
;;        (is (= "jack" (:first_name jack-details)))
;;        (is (= "jack" (:last_name jack-details)))))))

;; (deftest ^:integration test-friends-list
;;   (in-facebook-lab 
;;    gateway/APP-ID gateway/APP-SECRET
;;    (let [jack (test-user/create "jack")
;;          jill (test-user/create "jill")
;;          mary (test-user/create "mary")]
;;      (login-as jack)
;;      (test-user/make-friend jill)
;;      (test-user/make-friend mary)

;;      (let [friends-of-jack (gateway/friends-list (test-user/access-token))]
;;        (is (= 2 (count friends-of-jack)))
;;        ;;TODO We need to verify the return values in a better way
;;        (is (some #(= "jill" (:first_name %)) friends-of-jack))
;;        (is (some #(= "mary" (:first_name %)) friends-of-jack))))))



