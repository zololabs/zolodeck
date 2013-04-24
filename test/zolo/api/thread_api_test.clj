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
            [zolo.utils.calendar :as zolo-cal]
            [zolo.test.web-utils :as w-utils]))

(def REPLY-TO "reply_to")

(demonictest test-find-suggestion-sets
  (let [shy (shy-persona/create)
        vincent (vincent-persona/create)
        vincent-uid (-> vincent :user/user-identities first :identity/provider-uid)
        jack-uid (-> vincent :user/contacts second :contact/social-identities first :social/provider-uid)]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :get (str "/users/" (:user/guid shy) "/threads") {})]
        (is (= 404 (:status resp)))))

    (testing "when user is not present, it should return 404"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (random-guid-str) "/threads") {})]
        (is (= 404 (:status resp)))))

    (testing "when user with no messages is present, it should return empty"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (:user/guid shy) "/threads") {:action REPLY-TO})]
        (is (= 200 (:status resp)))
        (is (empty? (get-in resp [:body])))))

    (testing "when not passed the right action query, should return error"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/threads"))]
        (is (= 500 (:status resp)))))
    
    (testing "when user with 2 friends, and 1 reply-to thread, it should return the right thread"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/threads")
                                         {:action REPLY-TO})]
        (is (= 200 (:status resp)))

        (is (= 1 (count (get-in resp [:body]))))
        (is (= 1 (count (-> resp :body first :messages))))
        (is (= [vincent-uid] (-> resp :body first :messages first :to)))
        (is (= jack-uid (-> resp :body first :messages first :from)))
        (doseq [m (-> resp :body first :messages)]
          (has-keys m [:message_id :guid :provider :thread_id :from :to :date :text]))))))

