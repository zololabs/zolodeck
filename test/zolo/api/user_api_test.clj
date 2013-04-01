(ns zolo.api.user-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolodeck.utils.debug
        zolodeck.utils.clojure
        zolo.test.core-utils
        zolo.web.status-codes)
  (:require [zolo.domain.user :as user]
            [zolo.test.web-utils :as w-utils]
            [zolo.api.user-api :as user-api]
            [zolo.personas.factory :as personas]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]
            [zolodeck.utils.maps :as zmaps]
            [clojure.data.json :as json]
            [zolo.service.user-service :as u-service]
            [zolo.core :as server]))

(deftest test-new-user
  (demonic-testing "New User Signup - good request"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           resp (w-utils/web-request :post "/users" (personas/request-params mickey true))]
       (is (= 201 (:status resp)))
       (is (= "Mickey.Mouse@gmail.com" (get-in resp [:body :email]))))))

  (demonic-testing "New User Signup - bad request"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           resp (w-utils/web-request :post "/users" (personas/request-params {} true))]
       (is (= 400 (:status resp)))))))

(deftest test-get-user
  (demonic-testing "when user is not present it should return 404"
    (personas/in-social-lab
     (let [resp (w-utils/web-request :get (str "/users/" (random-guid-str)) {})]
       (is (= 404 (:status resp))))))

  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey (u-service/new-user (personas/request-params mickey true))
           resp (w-utils/web-request :get (str "/users/" (:user/guid d-mickey)) {})]

       (is (= 200 (:status resp)))
       (is (= (:user/email d-mickey) (get-in resp [:body :email])))
       (is (= (:user/guid d-mickey) (get-in resp [:body :guid])))))))

(deftest test-update-user
  (demonic-testing "when user is not present, it should return 404"
    (let [resp (w-utils/web-request :put (str "/users/" (random-guid-str)) {})]
      (is (= 404 (:status resp)))))

  (demonic-testing "when user is present, it should return updated distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey (u-service/new-user (personas/request-params mickey true))
           resp (w-utils/web-request :put (str "/users/" (:user/guid d-mickey)) {:permissions_granted false :login_tz "420"})]

       (is (= 200 (:status resp)))))))


(deftest test-find-users
  (demonic-testing "when no user is not present in db it should return 404"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           resp (w-utils/web-request :get "/users" (personas/request-params mickey true))]

       (is (= 404 (:status resp))))))

  (demonic-testing "when no matching user is present in db it should return 404"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           minie (fb-lab/create-user "Minie" "Mouse")
           d-mickey (u-service/new-user (personas/request-params mickey true))
           resp (w-utils/web-request :get "/users" (personas/request-params minie true))]

       (is (= 404 (:status resp))))))
    
  (demonic-testing "when user is present it should return distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey (u-service/new-user (personas/request-params mickey true))
           resp (w-utils/web-request :get "/users" (personas/request-params mickey true))]

       (is (= 200 (:status resp)))
       (is (= (:user/email d-mickey) (get-in resp [:body :email])))
       (is (= (:user/guid d-mickey) (get-in resp [:body :guid])))))))


