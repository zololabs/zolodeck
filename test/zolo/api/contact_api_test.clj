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
            [zolo.domain.message :as m]            
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.utils.calendar :as zcal]))

(defn- contacts-url [u c]
  (str "/users/" (or (:user/guid u) (random-guid-str))
       "/contacts/" (or (:contact/guid c) (random-guid-str))))

(defn- put-thread-url [u-guid ui-guid m-id]
  (str "/users/" u-guid "/ui/" ui-guid "/threads/" m-id))

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
  (let [reply-to-options {:selectors ["reply_to"] :thread_limit "50" :thread_offset "0"}
        shy (shy-persona/create)

        vincent (vincent-persona/create)
        vincent-uid (-> vincent :user/user-identities first :identity/provider-uid)
        vincent-ui-guid (-> vincent :user/user-identities first :identity/guid)
        
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
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/contacts") {:selectors ["junk"] :thread_limit "50" :thread_offset "0"})]
        (is (= 400 (:status resp)))))
    
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
            (has-keys m [:message_id :guid :provider :thread_id :from :to :date :text :snippet])))))

    (testing "after marking as done, reply-to contact shouldn't show up"
      (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/contacts") reply-to-options)
            last-m-id (-> resp :body first :reply_to_threads first :messages first :message_id)]
        (is (= 1 (count (get-in resp [:body]))))
        (w-utils/authed-request vincent :put (put-thread-url (:user/guid vincent) vincent-ui-guid last-m-id) {:done true})
        (let [resp (w-utils/authed-request vincent :get (str "/users/" (:user/guid vincent) "/contacts") reply-to-options)]
          (is (zero? (count (get-in resp [:body])))))))))

(demonictest test-find-reply-to-threads-pagination
  (let [friendly (pgen/generate {:SPECS {:first-name "Friendly"
                                         :last-name "Person"
                                         :friends [(pgen/create-friend-spec "F1" "L1" 1 1)
                                                   (pgen/create-friend-spec "F2" "L2" 1 3)
                                                   (pgen/create-friend-spec "F3" "L3" 1 5)
                                                   (pgen/create-friend-spec "F4" "L4" 1 7)
                                                   (pgen/create-friend-spec "F5" "L5" 1 9)
                                                   (pgen/create-friend-spec "F6" "L6" 1 11)
                                                   (pgen/create-friend-spec "F7" "L7" 1 13)
                                                   (pgen/create-friend-spec "F8" "L8" 1 15)
                                                   (pgen/create-friend-spec "F9" "L9" 1 17)]}})
        f-guid (:user/guid friendly)]

    (is (= 9 (count (get-in (w-utils/authed-request friendly :get (str "/users/" f-guid "/contacts")
                                                    {:selectors ["reply_to"] :thread_limit 50 :thread_offset 0}) [:body]))))

    (let [resp (get-in (w-utils/authed-request friendly :get (str "/users/" f-guid "/contacts")
                                               {:selectors ["reply_to"] :thread_limit 50 :thread_offset 0 :limit 5}) [:body])]
      (is (= 5 (count resp)))
      (same-value? ["F1" "F2" "F3" "F4" "F5"] (map :first_name resp)))

    (let [resp (get-in (w-utils/authed-request friendly :get (str "/users/" f-guid "/contacts")
                                               {:selectors ["reply_to"] :thread_limit 50 :thread_offset 0 :offset 7}) [:body])]
      (is (= 2 (count resp)))
      (same-value? ["F8" "F9"] (map :first_name resp)))

    (let [resp (get-in (w-utils/authed-request friendly :get (str "/users/" f-guid "/contacts")
                                               {:selectors ["reply_to"] :thread_limit 50 :thread_offset 0 :offset 3 :limit 3}) [:body])]
      (is (= 3 (count resp)))
      (same-value? ["F4" "F5" "F6"] (map :first_name resp)))))


(demonictest test-find-follow-up-contacts
  (let [follow-up-options {:selectors ["follow_up"] :thread_limit 50 :thread_offset 0}
        shy (shy-persona/create)
        winnie (pgen/generate {:SPECS {:first-name "Winnie"
                                       :last-name "Cooper"
                                       :friends [(pgen/create-friend-spec "Jack" "Daniels" 2 3)
                                                 (pgen/create-friend-spec "Jill" "Ferry" 1 2)]}})

        winnie-ui (-> winnie :user/user-identities first)
        winnie-uid (:identity/provider-uid winnie-ui)
        winnie-ui-guid (:identity/guid winnie-ui)

        jill-si (-> winnie :user/contacts first :contact/social-identities first)
        jill-sid (:social/provider-uid jill-si)
        
        jack-si (-> winnie :user/contacts second :contact/social-identities first)
        jack-sid (:social/provider-uid jack-si)]

    (testing "Unauthenticated user should be denied permission"
      (let [resp (w-utils/web-request :get (str "/users/" (:user/guid shy) "/contacts") follow-up-options)]
        (is (= 404 (:status resp)))))

    (testing "when user is not present, it should return 404"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (random-guid-str) "/contacts") follow-up-options)]
        (is (= 404 (:status resp)))))

    (testing "when user with no messages is present, it should return empty"
      (let [resp (w-utils/authed-request shy :get (str "/users/" (:user/guid shy) "/contacts") follow-up-options)]
        (is (= 200 (:status resp)))
        (is (empty? (get-in resp [:body])))))

    (testing "when not passed the right selector query, should return error"
      (let [resp (w-utils/authed-request winnie :get (str "/users/" (:user/guid winnie) "/contacts") {:selectors ["junk"] :thread_limit "50" :thread_offset 0})]
        (is (= 400 (:status resp)))))
    
    (testing "when user with 2 friends, and a follow-up thread each, it should return both"
      (let [resp (w-utils/authed-request winnie :get (str "/users/" (:user/guid winnie) "/contacts") follow-up-options)]
        (is (= 200 (:status resp)))

        (let [f-contacts (-> resp :body)
              [jack jill] (sort-by :first_name f-contacts)
              jill-f-threads (:follow_up_threads jill)
              jill-f-thread (-> jill-f-threads first)
              jill-f-message (-> jill-f-thread :messages first)
              lm-from-c (:lm_from_contact jill-f-thread)

              jack-f-threads (:follow_up_threads jack)
              jack-f-thread (-> jack-f-threads first)              
              jack-f-message (-> jack-f-thread :messages first)]

          (is (= 2 (count f-contacts)))
          (is (= 2 (count (:messages jill-f-thread))))
          (is (:guid jill-f-thread))
          (is (= (str "Conversation with " (:social/first-name jill-si) " " (:social/last-name jill-si)) (:subject jill-f-thread)))
          (assert-map-values jill-si [:social/first-name :social/last-name :social/photo-url]
                             lm-from-c [:first_name :last_name :picture_url])
          
          (is (= [jill-sid] (:to jill-f-message)))
          (is (= winnie-uid (:from jill-f-message)))

          (let [author (:author jill-f-message)]
            (is (= (:identity/first-name winnie-ui) (:first_name author)))
            (is (= (:identity/last-name winnie-ui) (:last_name author)))
            (is (= (:identity/photo-url winnie-ui) (:picture_url author))))

          (let [reply-tos (:reply_to jill-f-message)
                reply-to (first reply-tos)]
            (is (= (:social/first-name jill-si) (:first_name reply-to)))
            (is (= (:social/last-name jill-si) (:last_name reply-to)))
            (is (= (:social/provider-uid jill-si) (:provider_uid reply-to))))
          
          (doseq [m (:messages jill-f-thread)]
            (has-keys m [:message_id :guid :provider :thread_id :from :to :date :text :snippet]))

          (is (= 2 (count (:messages jack-f-thread))))
          
          (is (= [jack-sid] (:to jack-f-message)))
          (is (= winnie-uid (:from jack-f-message)))

          (let [author (:author jack-f-message)]
            (is (= (:identity/first-name winnie-ui) (:first_name author)))
            (is (= (:identity/last-name winnie-ui) (:last_name author)))
            (is (= (:identity/photo-url winnie-ui) (:picture_url author))))

          (let [reply-tos (:reply_to jack-f-message)
                reply-to (first reply-tos)]
            (is (= (:social/first-name jack-si) (:first_name reply-to)))
            (is (= (:social/last-name jack-si) (:last_name reply-to)))
            (is (= (:social/provider-uid jack-si) (:provider_uid reply-to))))

          (doseq [m (:messages jill-f-thread)]
            (has-keys m [:message_id :guid :provider :thread_id :from :to :date :text :snippet])))))
    
    (testing "when user has 2 friends, with a follow-up thread each, but one is marked for 2 days later, it should return just the one"
      (let [resp (w-utils/authed-request winnie :get (str "/users/" (:user/guid winnie) "/contacts") follow-up-options)
            last-r-m-id (->> resp :body first :follow_up_threads first :messages (remove :sent) first :message_id)
            three-days-later (-> (zcal/now-joda) (zcal/plus 3 :days) (zcal/dt->iso-string))]
        (is (= 2 (-> resp :body count)))

        (w-utils/authed-request winnie :put (put-thread-url (:user/guid winnie) winnie-ui-guid last-r-m-id) {:follow_up_on three-days-later})

        (let [resp (w-utils/authed-request winnie :get (str "/users/" (:user/guid winnie) "/contacts") follow-up-options)]
          (is (= 1 (-> resp :body count))))))))


(demonictest test-default-follow-up-behavior
  (let [follow-up-options {:selectors ["follow_up"] :thread_limit 50 :thread_offset 0}
        winnie (pgen/generate {:SPECS {:first-name "Winnie"
                                       :last-name "Cooper"
                                       :friends [(pgen/create-friend-spec "Jack" "Daniels" 2 3)
                                                 (pgen/create-friend-spec "Jill" "Ferry" 1 2)]}})]
    (run-as-of "2012-05-11"
      (testing "User has both 2 contacts, both have follow-up candidates, but only 1 day has passed: it should return neither"
        (let [resp (w-utils/authed-request winnie :get (str "/users/" (:user/guid winnie) "/contacts") follow-up-options)]
          (is (zero? (-> resp :body count))))))

        (run-as-of "2012-05-14"
      (testing "User has both 2 contacts, both have follow-up candidates, but only 3 days have passed: it should return both"
        (let [resp (w-utils/authed-request winnie :get (str "/users/" (:user/guid winnie) "/contacts") follow-up-options)]
          (is (= 2 (-> resp :body count))))))))
