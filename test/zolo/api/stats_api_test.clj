(ns zolo.api.stats-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        conjure.core
        zolo.test.core-utils
        zolo.utils.clojure
        zolo.demonic.test 
        zolo.demonic.core)
  (:require [zolo.personas.generator :as pgen]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.test.web-utils :as w-utils]))

(demonictest test-get-contact-stats
  (doseq  [u (pgen/generate-all {:SPECS
                                 {:friends [(pgen/create-friend-spec "Strong" "Contact" 50 50)
                                            (pgen/create-friend-spec "Medium" "Contact" 10 10)
                                            (pgen/create-friend-spec "Weak1" "Contact1" 5 5)
                                            (pgen/create-friend-spec "Weak2" "Contact2" 0 0)]}
                                 :UI-IDS-ALLOWED [:FACEBOOK]})]
    
    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :get (str "/users/" (:user/guid u) "/contact_stats") {})]
        (is (= 403 (:status resp)))))

    (testing "when user is not present, it should return 404"
      (let [resp (w-utils/authed-request u :get (str "/users/" (random-guid-str) "/contact_stats") {})]
        (is (= 404 (:status resp)))))

    (testing "when user is present, it should return contact stats"
      (run-as-of "2012-09-01"
        
        (let [resp (w-utils/authed-request u :get (str "/users/" (:user/guid u) "/contact_stats") {})]

          (is (= 200 (:status resp)))

          (is (= 4 (get-in resp [:body :total])))
          (is (= 1 (get-in resp [:body :strong])))
          (is (= 1 (get-in resp [:body :strong])))
          (is (= 2 (get-in resp [:body :weak])))

          (is (= 3 (get-in resp [:body :quartered]))))))))

