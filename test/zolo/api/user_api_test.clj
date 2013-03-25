(ns zolo.api.user-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolodeck.utils.debug
        zolo.test.core-utils
        zolo.web.status-codes)
  (:require [zolo.domain.user :as user]
            [zolo.test.web-utils :as w-utils]
            [zolo.api.user-api :as user-api]
            [zolo.personas.factory :as personas]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]
            [zolodeck.utils.maps :as zmaps]
            [clojure.data.json :as json]
            [zolo.core :as server]))

(deftest test-new-user
  (demonic-testing "New User Signup - good request"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           resp (w-utils/web-request :post "/users" (personas/request-params mickey true))]
       (is (= 201 (:status resp)))
       (is (= "Mickey.Mouse@gmail.com" (get-in resp [:body :email])))))))