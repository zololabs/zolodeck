(ns zolo.api.user-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.demonic.test
        zolo.demonic.core
        zolo.utils.debug
        zolo.utils.clojure
        zolo.test.core-utils
        zolo.web.status-codes)
  (:require [zolo.domain.user :as user]
            [zolo.domain.user-identity :as ui]
            [zolo.test.web-utils :as w-utils]
            [zolo.api.user-api :as user-api]
            [zolo.personas.factory :as personas]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [clojure.data.json :as json]
            [zolo.service.user-service :as u-service]
            [zolo.core :as server]
            [zolo.personas.generator :as pgen]
            [zolo.personas.factory :as personas]))

(deftest test-new-user
  (demonic-testing "New User Signup - good request"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           resp (w-utils/web-request :post "/users" (personas/fb-request-params mickey true))]
       (is (= 201 (:status resp)))
       (is (= ["Mickey.Mouse@gmail.com"] (get-in resp [:body :emails]))))))

  (demonic-testing "New User Signup - bad request"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           resp (w-utils/web-request :post "/users" (personas/fb-request-params {} true))]
       (is (= 400 (:status resp)))))))

(demonictest test-get-user
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})]

     (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :get (str "/users/" (:user/guid u)) {})]
       (is (= 404 (:status resp)))))
    
    (testing "when user is not present it should return 404"
      (let [resp (w-utils/authed-request u :get (str "/users/" (random-guid-str)) {})]
        (is (= 404 (:status resp)))))

    (testing "when user is present it should return distilled user"
      (let [resp (w-utils/authed-request u :get (str "/users/" (:user/guid u)) {})]

        (is (= 200 (:status resp)))
        (is (= (str (:user/guid u)) (get-in resp [:body :guid])))))))

(demonictest test-update-user
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request  :put (str "/users/" (:user/guid u)) (personas/user-request-params  u))]
        (is (= 404 (:status resp)))))

    (testing "when user is not present, it should return 404"
      (let [resp (w-utils/authed-request u  :put (str "/users/" (random-guid-str)) {})]
        (is (= 404 (:status resp)))))

    (testing "when user is present, it should return updated distilled user"
      (personas/in-social-lab
       (let [resp (w-utils/authed-request u  :put (str "/users/" (:user/guid u)) (personas/user-request-params  u))]

         (is (= 200 (:status resp)))
         (is (= (str (:user/guid u)) (get-in resp [:body :guid]))))))))


(demonictest test-find-users
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request  :get "/users" {:login_provider "FACEBOOK" :login_provider_uid (ui/fb-id u)})]
        (is (= 404 (:status resp)))))

    (testing "User has valid Signed Request but not a zolo user yet it should return 404"
      (let [resp (w-utils/authed-request {:user/user-identities [{:identity/provider :provider/facebook
                                                                  :identity/provider-uid "1000"}]}
                                         :get (str "/users")
                                         {:login_provider "FACEBOOK" :login_provider_uid (random-guid-str)})]
        (is (= 404 (:status resp)))))


    (testing "when no matching user is present in db it should return 404"
      (let [resp (w-utils/authed-request u :get "/users" {:login_provider "FACEBOOK" :login_provider_uid "JUNK"})]

        (is (= 404 (:status resp)))))
    
    (testing "when user is present it should return distilled user"
      (let [resp (w-utils/authed-request u :get "/users" {:login_provider "FACEBOOK" :login_provider_uid (ui/fb-id u)})]

        (is (= 200 (:status resp)))
        (is (= (ui/email-ids u) (get-in resp [:body :emails])))
        (is (= (str (:user/guid u)) (get-in resp [:body :guid])))))))


