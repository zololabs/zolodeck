(ns zolo.api.user-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.demonic.test
        zolo.scenario
        zolo.test.core-utils
        zolo.test.web-utils
        zolodeck.utils.debug
        zolo.web.status-codes))

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
        assert-user-not-present-in-datomic))

  )



