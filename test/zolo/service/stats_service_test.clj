(ns zolo.service.stats-service-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.service.suggestion-set-service :as ss-service]
            [zolo.domain.suggestion-set :as ss]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.contact :as contact]
            [zolo.personas.generator :as pgen]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.service.stats-service :as s-service]
            [zolo.domain.core :as d-core]
            [zolo.service.distiller.contact :as c-distiller]))

(deftest test-contact-stats
  (testing "When user is nil it should return nil"
    (run-as-of "2012-07-1"
      (is (nil? (s-service/contact-stats nil)))))

  (demonic-testing "When user is present, and has FB friends"
    (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Strong" "Contact" 50 50)
                                              (pgen/create-friend-spec "Medium" "Contact" 10 10)
                                              (pgen/create-friend-spec "Weak1" "Contact1" 5 5)
                                              (pgen/create-friend-spec "Weak2" "Contact2" 0 0)]}})
          u-guid (:user/guid u)
          [medium strong weak1 weak2] (sort-by contact/first-name (:user/contacts u))
          ibc (interaction/ibc u (:user/contacts u))]

      (run-as-of "2012-07-1"
        (let [c-stats (s-service/contact-stats u-guid)]
          (is (= 4 (:total c-stats)))
          (is (= 1 (:strong c-stats)))
          (is (= 1 (:medium c-stats)))
          (is (= 2 (:weak c-stats)))

          (is (= (d-core/run-in-tz-offset (:user/login-tz u) (c-distiller/distill strong u ibc))
                 (:strongest-contact c-stats)))
          (is (= (d-core/run-in-tz-offset (:user/login-tz u) (c-distiller/distill weak2 u ibc))
                 (:weakest-contact c-stats)))))

      (run-as-of "2014-07-01"
        (is (= 4 (:quartered (s-service/contact-stats u-guid)))))

      (run-as-of "2012-09-01"
        (is (= 3 (:quartered (s-service/contact-stats u-guid)))))
      
      (run-as-of "2012-07-01"
        (is (= 1 (:quartered (s-service/contact-stats u-guid)))))))


  (demonic-testing "When user is present, and has email friends"
    (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Strong" "Contact" 50 50)
                                              (pgen/create-friend-spec "Medium" "Contact" 10 10)
                                              (pgen/create-friend-spec "Weak1" "Contact1" 5 5)
                                              (pgen/create-friend-spec "Weak2" "Contact2" 1 1)
                                              (pgen/create-friend-spec "admin" "thoughtworks" 1 1)]}
                            :UI-IDS-ALLOWED [:EMAIL]})
          u-guid (:user/guid u)
          [medium strong weak1 weak2 admin] (sort-by contact/first-name (:user/contacts u))
          ibc (interaction/ibc u (contact/person-contacts u))]
      
      (run-as-of "2012-07-1"
        (let [c-stats (s-service/contact-stats u-guid)]
          (is (= 4 (:total c-stats)))
          (is (= 1 (:strong c-stats)))
          (is (= 1 (:medium c-stats)))
          (is (= 2 (:weak c-stats)))

          (is (= (d-core/run-in-tz-offset (:user/login-tz u) (c-distiller/distill strong u ibc))
                 (:strongest-contact c-stats)))
          (is (= (d-core/run-in-tz-offset (:user/login-tz u) (c-distiller/distill weak2 u ibc))
                 (:weakest-contact c-stats)))))

      (run-as-of "2014-07-01"
        (is (= 4 (:quartered (s-service/contact-stats u-guid)))))

      (run-as-of "2012-09-01"
        (is (= 3 (:quartered (s-service/contact-stats u-guid))))))))

