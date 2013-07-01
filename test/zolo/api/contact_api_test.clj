(ns zolo.api.contact-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.assertions.core
        zolo.utils.debug
        zolo.utils.clojure
        zolo.test.core-utils
        zolo.web.status-codes)
  (:require [zolo.domain.user :as user]
            [zolo.test.web-utils :as w-utils]
            [zolo.api.user-api :as user-api]
            [zolo.personas.factory :as personas]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [clojure.data.json :as json]
            [zolo.service.user-service :as u-service]
            [zolo.core :as server]
            [zolo.personas.generator :as pgen]
            [zolo.domain.core :as d-core]
            [zolo.domain.contact :as contact]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]))

(defn- contacts-url [u c]
  (str "/users/" (or (:user/guid u) (random-guid-str))
       "/contacts/" (or (:contact/guid c) (random-guid-str))))

(demonictest test-get-contact
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                             (pgen/create-friend-spec "Jill" "Ferry" 1 1)]}})
         [jack jill] (sort-by contact/first-name (:user/contacts u))]

     (testing "Unauthenticated user should be denied permission"
       (let [resp (w-utils/web-request :get (contacts-url u jack) {})]
         (is (= 404 (:status resp)))))
     
     (testing "when user is not present it should return nil"
       (let [resp (w-utils/authed-request u :get (contacts-url nil jack) {})]
         (is (= 404 (:status resp)))))

     (testing "when contact is not present it should return nil"
       (let [resp (w-utils/authed-request u :get (contacts-url u nil) {})]
         (is (= 404 (:status resp)))))

     (testing "when user and contact is present it should return distilled contact"
       (let [resp (w-utils/authed-request u :get (contacts-url u jack) {})]
         (is (= 200 (:status resp)))
         (is (= (str (:contact/guid jack)) (get-in resp [:body :guid]))))))))

(demonictest test-update-contact
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
        jack (first (:user/contacts u))]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :put (contacts-url u jack) {:muted true})]
        (is (= 404 (:status resp)))))
    
    (testing "When user is not present it should return 404"
      (let [resp (w-utils/authed-request u :put (contacts-url nil jack) {:muted true})]
          (is (= 404 (:status resp)))))
    
    (testing "When contact is not present it should return nil"
      (let [resp (w-utils/authed-request u :put (contacts-url u nil) {:muted true})]
        (is (= 404 (:status resp)))))
    
    (testing "When invalid contact is send it should throw exception"
      (let [resp (w-utils/authed-request u :put (contacts-url u jack) {:muted "JUNK"})]
        (is (= 400 (:status resp)))))
    
    (testing "When called with proper attributes it should update contact"
      (db-assert/assert-datomic-contact-count 1)
      (db-assert/assert-datomic-social-count 1)

      (is (not (contact/is-muted? jack)))
      
      (let [resp (w-utils/authed-request u :put (contacts-url u jack) {:muted true})]

        (db-assert/assert-datomic-contact-count 1)
        (db-assert/assert-datomic-social-count 1)
        
        (is (= 201 (:status resp)))

        (is (get-in resp [:body :muted]))
        (is (get-in resp [:body :person]))))))

(demonictest test-find-reply-to-contacts
  (let [reply-to-options {:selectors ["reply_to"]}
        shy (shy-persona/create)
        vincent (vincent-persona/create)
        vincent-uid (-> vincent :user/user-identities first :identity/provider-uid)
        jack-ui (-> vincent :user/contacts second :contact/social-identities first)
        jack-uid (:social/provider-uid jack-ui)]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :get (str "/users/" (:user/guid shy) "/contacts") reply-to-options)]
        (is (= 404 (:status resp)))))

    (testing "when user is not present, it should return 404"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (random-guid-str) "/contacts") reply-to-options)]
        (is (= 404 (:status resp)))))

    (testing "when user with no messages is present, it should return empty"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (:user/guid shy) "/contacts") reply-to-options)]
        (is (= 200 (:status resp)))
        (is (empty? (get-in resp [:body])))))

    (testing "when not passed the right selector query, should return error"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/contacts") {:junk "param"})]
        (is (= 500 (:status resp)))))
    
    (testing "when user with 2 friends, and 1 reply-to thread, it should return the friend who has reply to"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/contacts") reply-to-options)]
        (is (= 200 (:status resp)))

        (is (= 1 (count (get-in resp [:body]))))

        (let [r-contacts (-> resp :body)
              r-threads (-> r-contacts first :reply_to_threads)
              r-thread (-> r-threads first)
              r-message (-> r-thread  :messages first)
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