(ns zolo.domain.interaction-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.domain.core :as d-core]
            [zolo.domain.message :as message]
            [zolo.domain.interaction :as interaction]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.test.assertions.core :as c-assert]
            [zolo.domain.contact :as contact]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.personas.generator :as pgen]
            [zolo.utils.calendar :as zcal]
            [clj-time.coerce :as tcoerce]))

(deftest test-interactions-by-contacts
  (testing "when no messages are present"
    (let [shy (shy-persona/create-domain)
          imbc (message/messages-by-contacts shy (:user/contacts shy))
          ibc (interaction/interactions-by-contacts imbc)]

      (is (not (nil? ibc)))

      (is (= 2 (count ibc)))

      (is (= (set (:user/contacts shy)) (set (keys ibc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))]
        (is (empty? (ibc jack)))
        (is (empty? (ibc jill))))))

  (testing "when messages are present"
    (let [vincent (vincent-persona/create-domain)
          imbc (message/messages-by-contacts vincent (:user/contacts vincent))
          ibc (interaction/interactions-by-contacts imbc)]
      
      (is (not (nil? ibc)))

      (is (= 2 (count ibc)))

      (is (= (set (:user/contacts vincent)) (set (keys ibc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts vincent))]
        (is-not (empty? (ibc jack)))
        (is (= 2 (count (ibc jack))))
        
        (is-not (empty? (ibc jill)))
        (is (= 1 (count (ibc jill))))))))

(deftest test-ibc
  (testing "when no messages are present"
    (let [shy (shy-persona/create-domain)
          ibc (interaction/ibc shy (:user/contacts shy))]

      (is (not (nil? ibc)))

      (is (= 2 (count ibc)))

      (is (= (set (:user/contacts shy)) (set (keys ibc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))]
        (is (empty? (ibc jack)))
        (is (empty? (ibc jill))))))

  (testing "when messages are present"
    (let [vincent (vincent-persona/create-domain)
          ibc (interaction/ibc vincent (:user/contacts vincent))]
      
      (is (not (nil? ibc)))

      (is (= 2 (count ibc)))

      (is (= (set (:user/contacts vincent)) (set (keys ibc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts vincent))]
        (is-not (empty? (ibc jack)))
        (is (= 2 (count (ibc jack))))
        
        (is-not (empty? (ibc jill)))
        (is (= 1 (count (ibc jill))))))))

(deftest test-interaction-date
  (testing "When timezone offset is not set it should throw exception "
    (let [vincent (vincent-persona/create-domain)
          ibc (interaction/ibc vincent (:user/contacts vincent))
          i (d-core/run-in-gmt-tz (-> ibc interaction/interactions-from-ibc first))]
      (is (thrown-with-msg? RuntimeException #"User TZ is not set" (interaction/interaction-date i)))))

  (testing "When timezone offset is passed and "
    (testing "When no message is present it should return empty"
      (d-core/run-in-gmt-tz
       (is (nil? (interaction/interaction-date [])))))
    
    (testing "When only one message is present it should return the first message date"
      (d-core/run-in-gmt-tz
       (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
             ibc (interaction/ibc u (:user/contacts u))
             i (-> ibc interaction/interactions-from-ibc first)]
         (c-assert/assert-same-day? "2012-5-10" (interaction/interaction-date i)))))

    (testing "Date should be changed if tz is passed"
      (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
            ibc (interaction/ibc u (:user/contacts u))]
        (are [expected tz-offset]
          (c-assert/is-same-day? expected
                                 (d-core/run-in-tz-offset tz-offset
                                                          (-> ibc
                                                              interaction/interactions-from-ibc
                                                              first
                                                              interaction/interaction-date)))
          "2012-5-9"    420
          "2012-5-10"     0
          "2012-5-10"  -420)))))

(deftest test-interactions-from-ibc
  (testing "When timezone offset is not set it should throw exception "
    (let [vincent (vincent-persona/create-domain)
          ibc (interaction/ibc vincent (:user/contacts vincent))]
      (is (thrown-with-msg? RuntimeException #"User TZ is not set"
            (interaction/interactions-from-ibc ibc)))))

  (testing "When timezone offset is passed and "
    (testing "When no message is present it should return empty"
      (d-core/run-in-gmt-tz
       (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)]}})
             ibc (interaction/ibc u (:user/contacts u))]
         (is (empty? (interaction/interactions-from-ibc ibc))))))
    
    (testing "When only one interaction is present"
      (d-core/run-in-gmt-tz
       (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
             ibc (interaction/ibc u (:user/contacts u))
             interactions (interaction/interactions-from-ibc ibc)]
         (is (= 1 (count interactions)))
         (c-assert/assert-same-day? "2012-5-10" (interaction/interaction-date (first interactions))))))

    (testing "When many interactions are present it should sort them by date"
      (d-core/run-in-gmt-tz
       (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                                           (pgen/create-friend-spec "Jill" "Ferry" 2 3)]}})
             ibc (interaction/ibc u (:user/contacts u))
             interactions (interaction/interactions-from-ibc ibc)]
         (is (= 3 (count interactions)))
         
         (are [expected index]
           (c-assert/is-same-day? expected
                                  (-> interactions
                                      (nth index)
                                      interaction/interaction-date))
           "2012-5-10"   0
           "2012-5-10"   1
           "2012-5-11"   2))))))

(deftest test-daily-counts
  (testing "When timezone offset is not set it should throw exception "
    (let [vincent (vincent-persona/create-domain)
          ibc (interaction/ibc vincent (:user/contacts vincent))
          interactions (d-core/run-in-gmt-tz (interaction/interactions-from-ibc ibc))]
      (is (thrown-with-msg? RuntimeException #"User TZ is not set"
            (interaction/daily-counts interactions)))))

  (testing "When interaction happened should return empty"
    (run-as-of "2012-05-13"
      (d-core/run-in-gmt-tz
       (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)]}})
             ibc (interaction/ibc u (:user/contacts u))
             interactions (interaction/interactions-from-ibc ibc)]
         (is (empty? (interaction/daily-counts interactions)))))))

  (testing "When many interactions are happening it should also respect tz-offset"
    (run-as-of "2012-05-13"
      (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                                          (pgen/create-friend-spec "Jill" "Ferry" 2 5)
                                                          (pgen/create-friend-spec "Mary" "Fern"  3 12)
                                                          (pgen/create-friend-spec "Dont" "Care"  0 0)]}})
            ibc (interaction/ibc u (:user/contacts u))
            interactions (d-core/run-in-gmt-tz (interaction/interactions-from-ibc ibc))]
        
        (are [expected tz-offset] (=  expected
                                      (d-core/run-in-tz-offset tz-offset
                                                               (interaction/daily-counts interactions)))
             [["2012-05-09" 3] ["2012-05-10" 2] ["2012-05-11" 1] ["2012-05-12" 0] ["2012-05-13" 0]]
             420
             
             [["2012-05-10" 3] ["2012-05-11" 2] ["2012-05-12" 1] ["2012-05-13" 0]]
             0
             
             [["2012-05-10" 3] ["2012-05-11" 2] ["2012-05-12" 1] ["2012-05-13" 0]]
             -420
             
             [["2012-05-11" 3] ["2012-05-12" 2] ["2012-05-13" 1]]
             -1441)))))

;; (deftest test-update-inbox-messages
;;   (demonic-integration-testing  "First time user"
;;     (personas/in-social-lab
;;      (let [mickey (fb-lab/create-user "Mickey" "Mouse")
;;            donald (fb-lab/create-friend "Donald" "Duck")
;;            daisy (fb-lab/create-friend "Daisy" "Duck")
;;            db-mickey (in-demarcation (user/signup-new-user (personas/create-social-user mickey)))]
;;        (fb-lab/make-friend mickey donald)
;;        (fb-lab/make-friend mickey daisy)

;;        (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
;;              m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
;;              m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
             
;;              m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
;;              m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
         
;;          (fb-lab/login-as mickey)

;;          (in-demarcation
;;           (db-assert/assert-datomic-message-count 0))

;;          (in-demarcation
;;           (contact/update-contacts (user/reload db-mickey))
;;           (message/update-inbox-messages (user/reload db-mickey))
;;           (db-assert/assert-datomic-message-count 5))

;;          (in-demarcation)
;;          (let [[dm1 dm2 dm3 dm4 dm5] (sort-by dom/message-date (:user/messages (in-demarcation (user/reload db-mickey))))]
;;            (d-assert/messages-are-same m1 dm1)
;;            (d-assert/messages-are-same m2 dm2)
;;            (d-assert/messages-are-same m3 dm3)
;;            (d-assert/messages-are-same m4 dm4)
;;            (d-assert/messages-are-same m5 dm5)))))))


;; (deftest test-update-feed-messages-for-contact
;;   (demonic-integration-testing "Feeds should be updated"
;;     (personas/in-social-lab
;;      (let [mickey (fb-lab/create-user "Mickey" "Mouse")
;;            donald (fb-lab/create-friend "Donald" "Duck")
;;            daisy (fb-lab/create-friend "Daisy" "Duck")
;;            db-mickey (in-demarcation (user/signup-new-user (personas/create-social-user mickey)))]

;;        (fb-lab/make-friend mickey donald)
;;        (fb-lab/make-friend mickey daisy)
       
;;        (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
;;              m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
;;              m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
             
;;              m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
;;              m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")]
         
;;          (fb-lab/login-as mickey)

;;          (in-demarcation
;;           (db-assert/assert-datomic-message-count 0))

;;          (in-demarcation
;;           (contact/update-contacts (user/reload db-mickey))
;;           (message/update-inbox-messages (user/reload db-mickey))
;;           (db-assert/assert-datomic-message-count 5))

;;          (in-demarcation)
;;          (let [[dm1 dm2 dm3 dm4 dm5] (sort-by dom/message-date (:user/messages (in-demarcation (user/reload db-mickey))))]
;;            (d-assert/messages-are-same m1 dm1)
;;            (d-assert/messages-are-same m2 dm2)
;;            (d-assert/messages-are-same m3 dm3)
;;            (d-assert/messages-are-same m4 dm4)
;;            (d-assert/messages-are-same m5 dm5)))))))