(ns zolo.service.core-service-test
  (:use zolo.utils.debug
        clojure.test
        zolo.demonic.test)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.domain.message :as message]
            [zolo.store.user-store :as u-store]
            [zolo.store.message-store :as m-store]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.service.core :as service]))

(deftest test-provider-string->provider-enum
  (testing "When incorrect provider-string is send it should throw exeception"
    (is (thrown-with-msg? RuntimeException #"Unknown provider string specified: junk"
          (service/provider-string->provider-enum "junk"))))

  (testing "When correct provider-string is send it should return proper enum"
    (is (= :provider/facebook (service/provider-string->provider-enum "facebook")))))


(deftest test-validate-request!
  (testing "should throw exception when request is not valid"
    (is (thrown-with-msg? RuntimeException #":type :bad-request"
          (service/validate-request! {:a 1} {:a [:required :string]}))))

  (testing "should return params when it is valid"
    (is (= {:a 1} (service/validate-request! {:a 1} {:a [:required :integer]})))))