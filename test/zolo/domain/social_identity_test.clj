(ns zolo.domain.social-identity-test
  (:use zolo.utils.debug
        zolodeck.demonic.test
        zolodeck.demonic.core
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.social-identity :as si]))

;; (deftest test-update
;;   (demonic-testing "Should throw Runtime Exception when nil is passed"
;;     (is (thrown-with-msg? RuntimeException #"User Identity should not be nil" (user-identity/update nil {:identity/auth-token "abc"}))))

;;   (demonic-testing "Should get updated"
;;     (let [shy (shy-persona/create)
;;           shy-fb-ui (first (:user/user-identities shy))
;;           _ (user-identity/update shy-fb-ui {:identity/zip "94401"})
;;           after-update (first (:user/user-identities (user/reload shy)))]

;;       (is (not (= (:identity/zip shy-fb-ui) (:identity/zip after-update))))
;;       (is (= "94401" (:identity/zip after-update))))))