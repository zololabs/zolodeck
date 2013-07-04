(ns zolo.service.distiller.message-test
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
            [zolo.personas.generator :as pgen]
            [zolo.service.distiller.message :as m-distiller]))

(deftest test-distilled-messages
  (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Amrut" "Indya" 1 2)]}})
        u-ui (-> u :user/user-identities first)
        u-uid (u-ui :identity/provider-uid)
        amrut-ui (-> u :user/contacts first :contact/social-identities first)
        amrut-uid (:social/provider-uid amrut-ui)

        sm (->> u :user/messages first (m-distiller/distill u))
        rm (->> u :user/messages second (m-distiller/distill u))
        ]

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
        (is (= (:social/provider-uid amrut-ui) (:reply-to/provider-uid reply-to)))))

    (testing "when a temp message is created, it can be distilled properly"
      (let [tm (message/create-temp-message u-ui u-uid [amrut-uid] (:identity/provider u-ui) "thread-id" "subject" "text")
            dtm (m-distiller/distill u tm)]
        (is (= "text" (:message/text dtm)))
        (is (= [amrut-uid] (:message/to dtm)))
        (is (nil? (:message/done dtm)))
        (is (= u-uid (:message/from dtm)))
        (is (:message/sent dtm))
        (is (= "text" (:message/snippet dtm)))
        (is (= "subject" (:message/subject dtm)))
        (is (= "thread-id" (:message/thread-id dtm)))
        (is (= "Amrut" (-> dtm :message/reply-to first :reply-to/first-name)))
        (is (= "Indya" (-> dtm :message/reply-to first :reply-to/last-name)))
        (is (= amrut-uid (-> dtm :message/reply-to first :reply-to/provider-uid)))
        (is (= u-uid (-> dtm :message/reply-to first :reply-to/ui-provider-uid)))
        (is (= (:identity/provider u-ui) (:message/provider dtm)))
        (is (= (:identity/first-name u-ui) (get-in dtm [:message/author :author/first-name])))
        (is (= (:identity/last-name u-ui) (get-in dtm [:message/author :author/last-name])))
        (is (= (:identity/photo-url u-ui) (get-in dtm [:message/author :author/picture-url])))))))