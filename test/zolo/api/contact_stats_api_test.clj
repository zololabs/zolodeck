(ns zolo.api.contact-stats-api-test
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


(deftest test-get-contact-stats
  (demonic-testing "when user is not present, it should return 404"
    (let [resp (w-utils/web-request :get (str "/users/" (random-guid-str) "/contact_stats") {})]
      (is (= 404 (:status resp)))))

  (demonic-testing "when user is present, it should return contact stats"
    (run-as-of "2012-09-01"
      
      (let [u (pgen/generate {:SPECS
                              {:friends [(pgen/create-friend-spec "Strong" "Contact" 50 50)
                                         (pgen/create-friend-spec "Medium" "Contact" 10 10)
                                         (pgen/create-friend-spec "Weak1" "Contact1" 5 5)
                                         (pgen/create-friend-spec "Weak2" "Contact2" 0 0)]}})
            resp (w-utils/web-request :get (str "/users/" (:user/guid u) "/contact_stats") {})]

        (is (= 200 (:status resp)))

        (is (= 4 (get-in resp [:body :total])))
        (is (= 1 (get-in resp [:body :strong])))
        (is (= 1 (get-in resp [:body :strong])))
        (is (= 2 (get-in resp [:body :weak])))

        (is (= 3 (get-in resp [:body :quartered])))))))

