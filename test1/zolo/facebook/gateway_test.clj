(ns zolo.facebook.gateway-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.scenario
        zolo.test.core-utils
        zolo.test.web-utils
        zolo.utils.debug
        zolo.utils.test-utils
        zolo.web.status-codes)
  (:require [zolo.setup.config :as conf]
            [zolo.facebook.gateway :as gateway]
            [zolo.api.user-api :as user-api]))

(deftest ^:integration test-me
  (let [hobbes-details (gateway/me (hobbes-access-token))]
    (is (= "Hobbes" (:first_name hobbes-details)))
    (is (= "Tiger" (:last_name hobbes-details)))))

(deftest ^:integration test-friends-list
  (let [friends-of-hobbes (sort-by :first_name (gateway/friends-list (hobbes-access-token)))]
    (is (= 2 (count friends-of-hobbes)))
    (is (= "Calvin" (:first_name (first friends-of-hobbes))))
    (is (= "Susie" (:first_name (second friends-of-hobbes))))))



