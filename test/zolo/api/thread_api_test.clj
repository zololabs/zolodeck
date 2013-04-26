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

(demonictest test-find-reply-to-threads
  (let [shy (shy-persona/create)
        vincent (vincent-persona/create)
        vincent-uid (-> vincent :user/user-identities first :identity/provider-uid)
        jack-ui (-> vincent :user/contacts second :contact/social-identities first)
        jack-uid (:social/provider-uid jack-ui)]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :get (str "/users/" (:user/guid shy) "/threads") {})]
        (is (= 404 (:status resp)))))

    (testing "when user is not present, it should return 404"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (random-guid-str) "/threads") {})]
        (is (= 404 (:status resp)))))

    (testing "when user with no messages is present, it should return empty"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (:user/guid shy) "/threads") {:action zolo.service.thread-service/REPLY-TO})]
        (is (= 200 (:status resp)))
        (is (empty? (get-in resp [:body])))))

    (testing "when not passed the right action query, should return error"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/threads"))]
        (is (= 500 (:status resp)))))
    
    (testing "when user with 2 friends, and 1 reply-to thread, it should return the right thread"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/threads")
                                         {:action zolo.service.thread-service/REPLY-TO})]
        (is (= 200 (:status resp)))

        (is (= 1 (count (get-in resp [:body]))))

        (let [r-thread (-> resp :body first)
              r-message (-> r-thread :messages first)
              lm-from-c (:lm_from_contact r-thread)]
          (is (= 1 (count (:messages r-thread))))
          (is (:guid r-thread))
          (assert-map-values jack-ui [:social/first-name :social/last-name :social/photo-url]
                             lm-from-c [:first_name :last_name :picture_url])
          
          (is (= [vincent-uid] (:to r-message)))
          (is (= jack-uid (:from r-message)))

          (let [author (:author r-message)]
            (is (= (:social/first-name jack-ui) (:first_name author)))
            (is (= (:social/last-name jack-ui) (:last_name author)))
            (is (= (:social/photo-url jack-ui) (:picture_url author))))

          (let [reply-tos (:reply_to r-message)
                reply-to (first reply-tos)]
            (is (= (:social/first-name jack-ui) (:first_name reply-to)))
            (is (= (:social/last-name jack-ui) (:last_name reply-to)))
            (is (= (:social/provider-uid jack-ui) (:provider_uid reply-to))))
          
          (doseq [m (:messages r-thread)]
            (has-keys m [:message_id :guid :provider :thread_id :from :to :date :text :snippet])))))))

