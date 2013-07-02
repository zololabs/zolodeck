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
          (is (= (str "Conversation with " (:social/first-name jack-ui) " " (:social/last-name jack-ui)) (:subject r-thread)))
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


(demonictest test-find-follow-up-threads
  (let [shy (shy-persona/create)
        vincent (vincent-persona/create)
        vincent-ui (-> vincent :user/user-identities first)
        vincent-uid (:identity/provider-uid vincent-ui)
        jack-ui (-> vincent :user/contacts second :contact/social-identities first)
        jack-uid (:social/provider-uid jack-ui)]

    (testing "when user with no messages is present, it should return empty"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (:user/guid shy) "/threads") {:action zolo.service.thread-service/FOLLOW-UP})]
        (is (= 200 (:status resp)))
        (is (empty? (get-in resp [:body])))))
    
    (testing "when user with 2 friends, and 1 reply-to thread, it should return the right thread"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/threads")
                                         {:action zolo.service.thread-service/FOLLOW-UP})]
        (is (= 200 (:status resp)))

        (is (= 2 (count (get-in resp [:body]))))

        (let [f1-thread (-> resp :body second)
              f2-thread (-> resp :body first)
              f1-message (-> f1-thread :messages first)
              lm-from-c1 (:lm_from_contact f1-thread)]
          (is (= 2 (count (:messages f1-thread))))
          (is (:guid f1-thread))
          (is (= (str "Conversation with " (:social/first-name jack-ui) " " (:social/last-name jack-ui)) (:subject f1-thread)))
          (assert-map-values jack-ui [:social/first-name :social/last-name :social/photo-url]
                             lm-from-c1 [:first_name :last_name :picture_url])
          
          (is (= [jack-uid] (:to f1-message)))
          (is (= vincent-uid (:from f1-message)))

          (let [author (:author f1-message)]
            (is (= (:identity/first-name vincent-ui) (:first_name author)))
            (is (= (:identity/last-name vincent-ui) (:last_name author)))
            (is (= (:identity/photo-url vincent-ui) (:picture_url author))))

          (let [reply-tos (:reply_to f1-message)
                reply-to (first reply-tos)]
            (is (= (:social/first-name jack-ui) (:first_name reply-to)))
            (is (= (:social/last-name jack-ui) (:last_name reply-to)))
            (is (= (:social/provider-uid jack-ui) (:provider_uid reply-to))))
          
          (doseq [m (:messages f1-thread)]
            (has-keys m [:message_id :guid :provider :thread_id :from :to :date :text :snippet])))))))


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

(demonictest test-reply-to-after-marked-done
  (testing "Once you mark as done, thread shouldn't show up in reply-to view"
    (let [vincent (vincent-persona/create)
          vincent-uid (-> vincent :user/user-identities first :identity/guid)
          last-m-id (->> vincent :user/messages (reverse-sort-by m/message-date) first m/message-id)
          resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/threads") {:action zolo.service.thread-service/REPLY-TO})]
      (is (= 1 (count (get-in resp [:body]))))

      (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) vincent-uid last-m-id) {:done true})
      
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/threads") {:action zolo.service.thread-service/REPLY-TO})]
        (is (zero? (count (get-in resp [:body]))))))))