(ns zolo.api.user-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.demonic.test
        zolodeck.demonic.core
        zolodeck.utils.debug
        zolo.scenario
        zolo.test.web-utils
        zolo.test.core-utils
        zolo.web.status-codes)
  (:require [zolo.domain.user :as user]
            [zolo.api.user-api :as user-api]))

(deftest test-upsert-user
  '(demonic-testing "New User"
    (-> (new-scenario)
        login-as-valid-facebook-user
        post-new-user
        was-request-successful?
        assert-user-present-in-datomic))

  (demonic-testing "New user with invalid facebook login"
    (-> (new-scenario)
        assert-user-not-present-in-datomic
        post-new-user
        (was-response-status? (:forbidden STATUS-CODES))
        assert-user-not-present-in-datomic))

  )