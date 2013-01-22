(ns zolo.api.user-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.demonic.test
        zolodeck.utils.debug
        zolo.scenario
        zolo.test.web-utils)
  (:require [zolo.domain.user :as user]
            [zolo.api.user-api :as user-api]))

(zolo.setup.config/setup-config)
(zolo.setup.datomic-setup/init-datomic)

(deftest test-upsert-user
  (demonic-testing "New User"
    (-> (new-scenario)
        login-as-valid-facebook-user
        post-new-user
        was-request-successful?
        assert-user-present-in-datomic)))