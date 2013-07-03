(ns zolo.service.contact-service-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.clojure
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.message :as message]
            [zolo.domain.core :as d-core]
            [zolo.store.user-store :as u-store]
            [zolo.store.contact-store :as c-store]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.personas.generator :as pgen]))

(deftest test-update-contacts-for-user
  (demonic-testing "when user is not present, it should return nil"
    (is (nil? (c-service/update-contacts-for-user nil))))

  (demonic-testing  "User is present in the system and has NO contacts"
    (personas/in-social-lab
     (let [db-mickey (-> (fb-lab/create-user "Mickey" "Mouse")
                         personas/create-db-user-from-fb-user)]
       
       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (c-service/update-contacts-for-user db-mickey)
       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (is (= 0  (->> (c-service/update-contacts-for-user db-mickey)
                       :user/contacts
                       count))))))
  
  (demonic-testing  "User is present in the system and has contacts"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           donald (fb-lab/create-friend "Donald" "Duck")
           daisy (fb-lab/create-friend "Daisy" "Duck")
           minnie (fb-lab/create-friend "Minnie" "Mouse")
           db-mickey (-> mickey
                         personas/create-db-user-from-fb-user)]

       (fb-lab/make-friend mickey donald)
       (fb-lab/make-friend mickey daisy)       

       (db-assert/assert-datomic-contact-count 0)
       (db-assert/assert-datomic-social-count 0)
       
       (let [u-db-mickey (c-service/update-contacts-for-user db-mickey)]
         (db-assert/assert-datomic-contact-count 2)
         (db-assert/assert-datomic-social-count 2)
         
         (fb-lab/make-friend mickey minnie)
         
         (let [[db-daisy db-donald db-minnie] (->> (c-service/update-contacts-for-user u-db-mickey)
                                                   :user/contacts
                                                   (sort-by contact/first-name))]
           (db-assert/assert-datomic-contact-count 3)
           (db-assert/assert-datomic-social-count 3)
           
           (d-assert/contacts-are-same daisy db-daisy)
           (d-assert/contacts-are-same donald db-donald)
           (d-assert/contacts-are-same minnie db-minnie)))))))


(deftest test-update-scores
  (demonic-testing "Updating scores for all contacts"
    (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-friend "Donald" "Duck")
         daisy (fb-lab/create-friend "Daisy" "Duck")
         minnie (fb-lab/create-friend "Minnie" "Mouse")
         db-mickey (personas/create-db-user-from-fb-user mickey)]

     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)
     (fb-lab/make-friend mickey minnie)

     (let [m1 (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01 00:00")
           m2 (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02 00:00")
           m3 (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03 00:00")
           m4 (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01 00:00")
           m5 (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02 00:00")]
       
       (fb-lab/login-as mickey)

       (db-assert/assert-datomic-message-count 0)

       (let [refreshed-mickey (-> db-mickey
                                  c-service/update-contacts-for-user
                                  m-service/update-inbox-messages
                                  c-service/update-scores)]

         (db-assert/assert-datomic-message-count 5)

         (let [[daisy donald minnie] (->> refreshed-mickey
                                          :user/contacts
                                          (sort-by contact/first-name))]

           (is (= 20 (:contact/score daisy)))
           (is (= 30 (:contact/score donald)))
           (is (= 0 (:contact/score minnie))))))))))

(demonictest test-get-contact-by-guid
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                             (pgen/create-friend-spec "Jill" "Ferry" 1 1)]}})
         [jack jill] (sort-by contact/first-name (:user/contacts u))]
    
     (testing "when user is not present it should return nil"
       (is (nil? (c-service/get-contact-by-guid nil (:contact/guid jack)))))

     (testing "when contact is not present it should return nil"
       (is (nil? (c-service/get-contact-by-guid u (random-guid-str)))))

     (testing "when user and contact is present it should return distilled contact"
       
       (let [distilled-jack (c-service/get-contact-by-guid u (:contact/guid jack))]
         (is (not (nil? distilled-jack)))
         (is (= (:contact/guid jack) (:contact/guid distilled-jack))))))))

(demonictest test-update-contact
  (d-core/run-in-gmt-tz
   (run-as-of "2012-05-11"
     (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
           ibc (interaction/ibc u (:user/contacts u))
           jack (first (:user/contacts u))]

       (testing "When user is not present it should return nil"
         (is (nil? (c-service/update-contact nil jack {:muted true}))))
       
       (testing "When contact is not present it should return nil"
         (is (nil? (c-service/update-contact u nil {:muted true}))))
       
       (testing "When called with proper attributes it should update contact"
         (db-assert/assert-datomic-contact-count 1)
         (db-assert/assert-datomic-social-count 1)

         (is (not (contact/is-muted? jack)))
         
         (let [updated-jack (c-service/update-contact u jack {:muted true})]

           (db-assert/assert-datomic-contact-count 1)
           (db-assert/assert-datomic-social-count 1)
           
           (is (contact/is-muted? updated-jack))
           (is (contact/is-muted? (c-store/find-by-guid (:contact/guid jack))))

           (is (= (dissoc (contact/distill jack ibc) :contact/muted)
                  (dissoc updated-jack :contact/muted))))

         (let [updated-jack (c-service/update-contact u jack {:person true :muted false})]
            
            (db-assert/assert-datomic-contact-count 1)
            (db-assert/assert-datomic-social-count 1)
            
            (is (contact/is-a-person? updated-jack))
            (is (contact/is-a-person? (c-store/find-by-guid (:contact/guid jack))))
            
            (is (= (dissoc (contact/distill jack ibc) :contact/is-a-person)
                   (dissoc updated-jack :contact/is-a-person)))))))))


(deftest test-find-reply-to-contacts

  (let [reply-to-options {:selectors ["reply_to"]}]
    
    (demonic-testing "User is not present, it should return nil"
      (is (empty? (c-service/list-contacts nil reply-to-options))))
    
    (demonic-testing "User present, but has no messages, it should return empty"
      (let [shy (shy-persona/create)
            contacts (c-service/list-contacts (:user/guid shy) reply-to-options)]
        (is (empty? contacts))))
    
    (demonic-testing "User has both a reply-to and a replied-to contact, it should return the reply-to contact"
      (let [vincent (vincent-persona/create)
            vincent-ui (-> vincent :user/user-identities first)
            vincent-uid (:identity/provider-uid vincent-ui)
            jack-ui (->> vincent
                         :user/contacts
                         (sort-by :contact/first-name)
                         first
                         :contact/social-identities
                         first)
            jack-uid (:social/provider-uid jack-ui)
            
            reply-to-contacts (c-service/list-contacts vincent reply-to-options)
            reply-threads (-> reply-to-contacts first :reply-to-threads)
            
            r-messages (-> reply-threads first :thread/messages)
            last-m (first r-messages)]
        
        (is (= 1 (count reply-to-contacts)))
        (is (= 1 (count reply-threads)))
        
        (is (= (str "Conversation with " (:social/first-name jack-ui) " " (:social/last-name jack-ui))
               (-> reply-threads first :thread/subject)))
        
        (is (= 1 (count r-messages)))
        (is (= jack-uid (:message/from last-m)))
        (is (= #{vincent-uid} (:message/to last-m)))
        (is (:message/snippet last-m))
        (is-not (:message/sent last-m))
        
        (let [lm-from-c (-> reply-threads first :thread/lm-from-contact)]
          (is (= (:social/first-name jack-ui) (:contact/first-name lm-from-c)))
          (is (= (:social/last-name jack-ui) (:contact/last-name lm-from-c)))
          (is (= (:social/photo-url jack-ui) (:contact/picture-url lm-from-c))))
        
        (let [author (:message/author last-m)]
          (is (= (:social/first-name jack-ui) (:author/first-name author)))
          (is (= (:social/last-name jack-ui) (:author/last-name author)))
          (is (= (:social/photo-url jack-ui) (:author/picture-url author))))
        
        (let [reply-tos (:message/reply-to last-m)
              reply-to (first reply-tos)]
          (is (= (:social/first-name jack-ui) (:reply-to/first-name reply-to)))
          (is (= (:social/last-name jack-ui) (:reply-to/last-name reply-to)))
          (is (= (:social/provider-uid jack-ui) (:reply-to/provider-uid reply-to))))
        
        (testing "after replying to this thread, it shouldn't be a reply-to thread"
          (let [r-message (first r-messages)
                _ (mocking [fb-chat/send-message]
                    (m-service/new-message vincent {:text "Hey hello" :provider "facebook"
                                                    :guid (-> vincent :user/guid str)
                                                    :from vincent-uid
                                                    :to [(-> r-message :message/reply-to first :reply-to/provider-uid)]
                                                    :thread_id (:message/thread-id r-message)}))
                updated-r-threads (c-service/list-contacts (:user/guid vincent) reply-to-options)]
            (is (= 1 (count reply-threads)))
            (is (empty? updated-r-threads))))))
    
    (demonic-testing "When user has 1 friend, with 2  reply-to  threads, it should return both threads"
      (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 2 10)]}})
            jack (->> u :user/contacts first)
            jack-ui (-> jack :contact/social-identities first)
            
            reply-to-contacts (c-service/list-contacts u reply-to-options)
            thread-contact (-> reply-to-contacts first)
            reply-threads (-> reply-to-contacts first :reply-to-threads)]
        
        (is (= 1 (count reply-to-contacts)))
        (is (= 2 (count reply-threads)))
        
        (is (= (:social/first-name jack-ui) (:contact/first-name thread-contact)))
        (is (= (:social/last-name jack-ui) (:contact/last-name thread-contact)))
        (is (= (:social/photo-url jack-ui) (:contact/picture-url thread-contact)))))

    (demonic-testing "When user has 2 friends, with reply-to  threads, it should return both contacts"
      (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 2 10)
                                                (pgen/create-friend-spec "Jill" "Ferry" 2 10)]}})
            
            reply-to-contacts (c-service/list-contacts u reply-to-options)]
        
        (is (= 2 (count reply-to-contacts)))))))


(deftest test-find-follow-up-contacts

  (let [follow-up-options {:selectors ["follow_up"]}]
    
    (demonic-testing "User is not present, it should return nil"
      (is (empty? (c-service/list-contacts nil follow-up-options))))
    
    (demonic-testing "User present, but has no messages, it should return empty"
      (let [shy (shy-persona/create)]
        (is (empty? (c-service/list-contacts shy follow-up-options)))))
    
    (demonic-testing "User has both 2 contacts, that both have follow-up threads, it should return both the contacts"
      (let [vincent (vincent-persona/create)
            vincent-ui (-> vincent :user/user-identities first)
            vincent-uid (:identity/provider-uid vincent-ui)

            [jack jill] (->> vincent :user/contacts (sort-by :contact/first-name))
            
            jill-ui (-> jill :contact/social-identities first)
            jill-uid (:social/provider-uid jill-ui)
            
            jack-ui (-> jack :contact/social-identities first)
            jack-uid (:social/provider-uid jack-ui)
            
            follow-contacts (c-service/list-contacts vincent follow-up-options)
            follow-threads-jill (-> follow-contacts first :follow-up-threads)
            follow-threads-jack (-> follow-contacts second :follow-up-threads)
            
            jill-messages (-> follow-threads-jill first :thread/messages)          
            jack-messages (-> follow-threads-jack first :thread/messages)
            
            jack-last-m (first jack-messages)
            jill-last-m (first jill-messages)]

        (is (= 2 (count follow-contacts)))
        
        (is (= (str "Conversation with " (:social/first-name jill-ui) " " (:social/last-name jill-ui))
               (-> follow-threads-jill first :thread/subject)))
        (is (= (str "Conversation with " (:social/first-name jack-ui) " " (:social/last-name jack-ui))
               (-> follow-threads-jack first :thread/subject)))
        
        (is (= 2 (count jack-messages)))
        (is (= 2 (count jill-messages)))
        (is (= vincent-uid (:message/from jack-last-m)))
        (is (= #{jack-uid} (:message/to jack-last-m)))
        (is (:message/snippet jack-last-m))
        (is (:message/sent jack-last-m))
        
        (let [lm-from-c (-> follow-threads-jack first :thread/lm-from-contact)]
          (is (= (:social/first-name jack-ui) (:contact/first-name lm-from-c)))
          (is (= (:social/last-name jack-ui) (:contact/last-name lm-from-c)))
          (is (= (:social/photo-url jack-ui) (:contact/picture-url lm-from-c))))
        
        (let [author (:message/author jack-last-m)]
          (is (= (:identity/first-name vincent-ui) (:author/first-name author)))
          (is (= (:identity/last-name vincent-ui) (:author/last-name author)))
          (is (= (:identity/photo-url vincent-ui) (:author/picture-url author))))
        
        (let [reply-tos (:message/reply-to jack-last-m)
              reply-to (first reply-tos)]
          (is (= (:social/first-name jack-ui) (:reply-to/first-name reply-to)))
          (is (= (:social/last-name jack-ui) (:reply-to/last-name reply-to)))
          (is (= (:social/provider-uid jack-ui) (:reply-to/provider-uid reply-to))))))))
