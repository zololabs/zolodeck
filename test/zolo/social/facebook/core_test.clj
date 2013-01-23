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

(deftest test-signup-user
  (lab/in-facebook-lab
   (stubbing [fb-gateway/extended-user-info lab/extended-user-info]
     (let [mickey (lab/create-user "Mickey" "Mouse")
           params (request-params mickey)
           cookies {}
           canonical-user (social/signup-user params cookies)]
       (assert-basic-user-info mickey canonical-user)
       (assert-user-identity mickey (first (:user/user-identities canonical-user)))))))

