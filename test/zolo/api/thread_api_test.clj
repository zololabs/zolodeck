(ns zolo.api.thread-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        conjure.core
        zolo.test.core-utils
        zolo.test.assertions.core
        zolo.utils.clojure
        zolo.demonic.test
        zolo.demonic.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.domain.message :as m]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.test.web-utils :as w-utils]))

(defn- put-thread-url [u-guid ui-guid m-id]
  (str "/users/" u-guid "/ui/" ui-guid "/threads/" m-id))

(demonictest test-mark-as-done
  (let [vincent (vincent-persona/create)
        shy (shy-persona/create)
        vincent-uid (-> vincent :user/user-identities first :identity/guid)
        last-m-id (->> vincent :user/messages (reverse-sort-by m/message-date) first m/message-id)]

    (testing "User is not present, it should return nil"
      (let [resp (w-utils/authed-request vincent :put (put-thread-url (random-guid-str) vincent-uid last-m-id) {:done true})]
        (is (= 404 (:status resp)))))

    (testing "User Identity is not present, it should return nil"
      (let [resp (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) (random-guid-str) last-m-id) {:done true})]
        (is (= 404 (:status resp)))))

    (testing "User Identity of a different user is used, it should return nil"
      (let [resp (w-utils/authed-request shy :put (put-thread-url (:user/guid shy) vincent-uid  last-m-id) {:done true})]
        (is (= 404 (:status resp)))))

    (testing "Message not present, it should return nil"
      (let [resp (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) vincent-uid (random-guid-str)) {:done true})]
        (is (= 404 (:status resp)))))

    (testing "Both user and message present, should return message as done"
      (let [resp (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) vincent-uid last-m-id) {:done true})]
        (is (= 200 (:status resp)))
        (is (get-in resp [:body :done]))))))


(demonictest test-mark-follow-up
  (let [vincent (vincent-persona/create)
        shy (shy-persona/create)
        vincent-uid (-> vincent :user/user-identities first :identity/guid)]

    (testing "Both user and received message present, should return message with follow-up set"
      (let [r-m-id (->> vincent :user/messages (filter #(m/is-received-by-user? vincent %)) (reverse-sort-by m/message-date) first m/message-id)
            resp (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) vincent-uid r-m-id) {:follow_up_on "2013-07-03T16:58:04.003Z"})]
        (is (= 200 (:status resp)))
        (is (= "2013-07-03 16:58" (get-in resp [:body :follow_up_on])))))

    (testing "Both user and received message present, shouldn't disturb done-ness if only follow-up is set"
      (let [r-m-id (->> vincent :user/messages (filter #(m/is-received-by-user? vincent %)) (reverse-sort-by m/message-date) first m/message-id)
            _ (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) vincent-uid r-m-id) {:done true})
            resp (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) vincent-uid r-m-id) {:follow_up_on "2013-07-03T16:58:04.003Z"})]
        (is (= 200 (:status resp)))
        (is (= "2013-07-03 16:58" (get-in resp [:body :follow_up_on])))
        (is (get-in resp [:body :done]))))))