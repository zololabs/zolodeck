(ns zolo.api.user-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.demonic.test
        zolo.scenario
        zolo.test.core-utils
        zolo.test.web-utils
        zolo.utils.test-utils
        zolo.utils.debug
        zolo.utils.test
        zolo.demonic.core
        zolo.web.status-codes)
  (:require [zolo.setup.config :as conf]
            [zolo.setup.datomic-schema :as schema]
            [zolo.facebook.gateway :as fb-gateway]
            [zolo.domain.user :as user]
            [zolo.api.user-api :as user-api]
            [com.georgejahad.difform :as df]
            [zolo.domain.zolo-graph :as zg]
            [zolo.viz.d3 :as d3]))

(deftest test-upsert-user
  (demonic-testing "New User"
    (-> (new-scenario)
        assert-user-not-present-in-datomic
        login-as-valid-facebook-user
        post-new-user
        was-request-successful?
        assert-user-present-in-datomic))
  
  (demonic-testing "New user with invalid facebook login"
    (-> (new-scenario)
        assert-user-not-present-in-datomic
        post-new-user
        (was-response-status? (:found STATUS-CODES))
        assert-user-not-present-in-datomic))) 


(deftest ^:integration test-fully-loaded-user
  (let [hobbes (in-demarcation
                (-> (hobbes-access-token)
                    fb-gateway/me
                    user/insert-fb-user
                    user-api/fully-loaded-user))
        hobbes-reloaded (in-demarcation
                         (user/reload hobbes))]
    
    (is (= (count (:user/contacts hobbes)) (count (:user/contacts hobbes-reloaded))))
    
    (is (= 6
           (count (mapcat :contact/messages (:user/contacts hobbes)))
           (count (mapcat :contact/messages (:user/contacts hobbes-reloaded)))))

    (is (= (zg/user->zolo-graph hobbes)
           (zg/user->zolo-graph hobbes-reloaded)))

    (is (= (d3/format-for-d3 (zg/user->zolo-graph hobbes))
           (d3/format-for-d3 (zg/user->zolo-graph hobbes-reloaded))))

    (re-initialize-db (conf/datomic-db-name) @schema/SCHEMA-TX)))