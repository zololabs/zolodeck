(ns zolo.facebook.inbox-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.scenario
        zolo.test.core-utils
        zolo.test.web-utils
        zolodeck.utils.debug
        zolo.utils.test-utils
        zolo.web.status-codes
        zolodeck.clj-social-lab.facebook.core)
  (:require ;;[zolodeck.clj-social-lab.facebook.user :as test-user]
            [zolo.facebook.inbox :as inbox]
            [zolo.api.user-api :as user-api]))

;; (deftest ^:integration test-fetch-inbox-threads
;;   (let [jack (test-user/create "jack")
;;         jill (test-user/create "jill")
;;         mary (test-user/create "mary")]
;;     (login-as jack)
;;     (test-user/make-friend jill)
;;     (test-user/make-friend mary)))



