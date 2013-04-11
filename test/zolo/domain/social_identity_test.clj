(ns zolo.domain.social-identity-test
  (:use zolo.utils.debug
        zolo.demonic.test
        zolo.demonic.core
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.social-identity :as si]
            [zolo.personas.generator :as pgen]))

(deftest test-not-a-person
  (testing "when an SI has a clean email-address"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Lucky" "Strike" 1 1)
                                                     (pgen/create-friend-spec "Mighty" "Mouse" 2 5)
                                                     (pgen/create-friend-spec "Bat" "Man" 3 10)
                                                     (pgen/create-friend-spec "donotreply" "Man" 5 20)
                                                     (pgen/create-friend-spec "R2" "D2" 7 30)]}
                                   :UI-IDS-ALLOWED [:FACEBOOK :EMAIL]
                                   :UI-IDS-COUNT 3})]
      (print-vals (-> u :user/contacts first :contact/social-identities first)))
    (print-vals ">>>>>>>>>>>>>")
    ;; (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Lucky" "Strike" 1 1)]}})]
    ;;   (print-vals (-> u :user/contacts first :contact/social-identities first)))
    )

  (testing "when an SI has a suspect email-address"
    ))

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