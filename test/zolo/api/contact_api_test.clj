(ns zolo.api.contact-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.demonic.test
        zolo.demonic.core
        zolo.utils.debug
        zolo.utils.clojure
        zolo.test.core-utils
        zolo.web.status-codes)
  (:require [zolo.domain.user :as user]
            [zolo.test.web-utils :as w-utils]
            [zolo.api.user-api :as user-api]
            [zolo.personas.factory :as personas]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [clojure.data.json :as json]
            [zolo.service.user-service :as u-service]
            [zolo.core :as server]
            [zolo.personas.generator :as pgen]
            [zolo.domain.core :as d-core]
            [zolo.domain.contact :as contact]))

(defn- contacts-url [u c]
  (str "/users/" (or (:user/guid u) (random-guid-str))
       "/contacts/" (or (:contact/guid c) (random-guid-str))))

(demonictest test-get-user
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                             (pgen/create-friend-spec "Jill" "Ferry" 1 1)]}})
         [jack jill] (sort-by contact/first-name (:user/contacts u))]
     
     (testing "when user is not present it should return nil"
       (let [resp (w-utils/web-request :get (contacts-url nil jack) {})]
         (is (= 404 (:status resp)))))

     (testing "when contact is not present it should return nil"
       (let [resp (w-utils/web-request :get (contacts-url u nil) {})]
         (is (= 404 (:status resp)))))

     (testing "when user and contact is present it should return distilled contact"
       (let [resp (w-utils/web-request :get (contacts-url u jack) {})]
         (is (= 200 (:status resp)))
         (is (= (str (:contact/guid jack)) (get-in resp [:body :guid]))))))))


