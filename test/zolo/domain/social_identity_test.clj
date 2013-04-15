(ns zolo.domain.social-identity-test
  (:use zolo.utils.debug
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils        
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.social-identity :as si]
            [zolo.personas.generator :as pgen]))

(deftest test-not-a-person
  (testing "when SIs have clean email-addresses"
    (doseq [u (pgen/generate-domain-all {:SPECS {:friends [(pgen/create-friend-spec "Lucky" "Strike" 1 1)
                                                           (pgen/create-friend-spec "Mighty" "Mouse" 2 5)
                                                           (pgen/create-friend-spec "Bat" "Man" 3 10)
                                                           (pgen/create-friend-spec "Hello" "Man" 5 20)
                                                           (pgen/create-friend-spec "R2" "D2" 7 30)]}
                                         :UI-IDS-ALLOWED [:EMAIL :FACEBOOK]
                                         :UI-IDS-COUNT 2})]
      (let [sis (->> u :user/contacts (mapcat :contact/social-identities))]
        (is (= 5 (count sis)))
        (doseq [esi (filter si/is-email? sis)]
          (is (si/is-a-person esi))))))

  (testing "when an SI has a suspect email-addresses"
    (doseq [u (pgen/generate-domain-all {:SPECS {:friends [(pgen/create-friend-spec "Lucky" "Strike" 1 1)
                                                           (pgen/create-friend-spec "Mighty" "Mouse" 2 5)
                                                           (pgen/create-friend-spec "Bat" "Man" 3 10)
                                                           (pgen/create-friend-spec "donotreply" "Man" 5 20)
                                                           (pgen/create-friend-spec "R2" "D2" 7 30)]}
                                         :UI-IDS-ALLOWED [:EMAIL :FACEBOOK]
                                         :UI-IDS-COUNT 2})]
      (let [sis (->> u :user/contacts (mapcat :contact/social-identities))
            esis (filter si/is-email? sis)]
        (is (= 5 (count sis)))
        (doseq [esi esis]
          (if (= "donotreply@Man.com" (:social/provider-uid esi))
            (is-not (si/is-a-person esi))
            (is (si/is-a-person esi))))))))

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