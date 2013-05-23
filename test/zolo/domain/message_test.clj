(ns zolo.domain.message-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.test.assertions.core
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.domain.message :as message]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.domain.contact :as contact]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.core :as d-core]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.vincent :as vincent-persona]
            [zolo.personas.generator :as pgen]))

(deftest test-inbox-messages-by-contacts
  (testing "when no messages are present"
    (let [shy (shy-persona/create-domain)
          imbc (message/messages-by-contacts shy)]

      (is (not (nil? imbc)))

      (is (= 2 (count imbc)))

      (is (= (set (:user/contacts shy)) (set (keys imbc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))]
        (is (empty? (imbc jack)))
        (is (empty? (imbc jill))))))

  (testing "when messages are present"
    (let [vincent (vincent-persona/create-domain)
          imbc (message/messages-by-contacts vincent)]
      
      (is (not (nil? imbc)))

      (is (= 2 (count imbc)))

      (is (= (set (:user/contacts vincent)) (set (keys imbc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts vincent))]
        (is-not (empty? (imbc jack)))
        (is (= 3 (count (imbc jack))))
        
        (is-not (empty? (imbc jill)))
        (is (= 2 (count (imbc jill))))))))

(deftest test-last-sent-message
  (testing "When there is no messages it should return nil"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels")]}})
          jack (first (:user/contacts u))]
      (is (nil? (message/last-sent-message jack nil)))
      (is (nil? (message/last-sent-message jack [])))))

  (testing "When there is no sent messages it should return nil"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
          jack (first (:user/contacts u))
          msgs (-> u interaction/ibc interaction/messages-from-ibc)]
      (is (nil? (message/last-sent-message jack msgs)))))

  (testing "When only temp message is present it should return the last temp message")

  (testing "When only regular message is present it should return the last regular message"
    (d-core/run-in-gmt-tz
      (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 5 15)]}})
            jack (first (:user/contacts u))
            msgs (-> u interaction/ibc interaction/messages-from-ibc)
            l-msg (message/last-sent-message jack msgs)]
        (is (not (nil? l-msg)))
        (is (= #inst "2012-05-14T00:02:00.000000000-00:00" (message/message-date l-msg))))))

  (testing "When temp and regular messages are present it should return the last sent message")) 

(deftest test-distilled-messages
  (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Amrut" "Indya" 1 2)]}})
        u-ui (-> u :user/user-identities first)
        u-uid (u-ui :identity/provider-uid)
        amrut-ui (-> u :user/contacts first :contact/social-identities first)
        amrut-uid (:social/provider-uid amrut-ui)

        rm (->> u :user/messages first (message/distill u))
        sm (->> u :user/messages second (message/distill u))]

    (testing "basic information should be set on distilled messages"
      (doseq [m [sm rm]]
        (has-keys m [:message/message-id :message/guid :message/provider :message/thread-id
                     :message/from :message/to :message/date :message/text :message/snippet :message/sent])))

    (testing "when user himself sent a message, :message/sent should be set to true, and author should be user"
      (is (:message/sent sm))
      (is (= (:identity/first-name u-ui) (get-in sm [:message/author :author/first-name])))
      (is (= (:identity/last-name u-ui) (get-in sm [:message/author :author/last-name])))
      (is (= (:identity/photo-url u-ui) (get-in sm [:message/author :author/picture-url]))))

    (testing "when user himself sent a message, reply-tos should reflect the contact"
      (let [reply-tos (sm :message/reply-to)
            reply-to (first reply-tos)]
        (is (= 1 (count reply-tos)))
        (is (= (:social/first-name amrut-ui) (:reply-to/first-name reply-to)))
        (is (= (:social/last-name amrut-ui) (:reply-to/last-name reply-to)))
        (is (= (:social/provider-uid amrut-ui) (:reply-to/provider-uid reply-to)))))

    (testing "when user received a message, :message/sent should be set to false, and author should be contact"
      (is-not (:message/sent rm))
      (is (= (:social/first-name amrut-ui) (get-in rm [:message/author :author/first-name])))
      (is (= (:social/last-name amrut-ui) (get-in rm [:message/author :author/last-name])))
      (is (= (:social/photo-url amrut-ui) (get-in rm [:message/author :author/picture-url]))))

    (testing "when user himself sent a message, reply-tos should reflect the contact"
      (let [reply-tos (rm :message/reply-to)
            reply-to (first reply-tos)]
        (is (= 1 (count reply-tos)))
        (is (= (:social/first-name amrut-ui) (:reply-to/first-name reply-to)))
        (is (= (:social/last-name amrut-ui) (:reply-to/last-name reply-to)))
        (is (= (:social/provider-uid amrut-ui) (:reply-to/provider-uid reply-to)))))))