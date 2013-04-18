(ns zolo.api.suggestion-set-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        conjure.core
        zolo.test.core-utils
        zolo.utils.clojure
        zolo.demonic.test
        zolo.demonic.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.personas.shy :as shy-persona]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.test.web-utils :as w-utils]))

(demonictest test-find-suggestion-sets
  (let [shy (shy-persona/create)]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :get (str "/users/" (:user/guid shy) "/suggestion_sets") {})]
        (is (= 403 (:status resp)))))
        
    (testing "when user is not present, it should return 404"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (random-guid-str) "/suggestion_sets") {})]
        (is (= 404 (:status resp)))))

    (testing "when user is present, it should return distilled ss"
      (run-as-of "2012-12-21"
        
        (let [resp (w-utils/authed-request shy :get (str "/users/" (:user/guid shy) "/suggestion_sets") {})]

          (is (= 200 (:status resp)))

          (is (= "ss-2012-12-21" (get-in resp [:body :name])))

          (is (= 2 (count (get-in resp [:body :contacts])))))))))

