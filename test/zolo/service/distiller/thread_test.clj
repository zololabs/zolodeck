(ns zolo.service.distiller.thread-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.test.assertions.core
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.core :as d-core]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.thread :as t]
            [zolo.domain.message :as m]
            [zolo.personas.generator :as pgen]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.distiller.thread :as t-distiller]))

(deftest test-distilled-threads
  (testing "When user has a thread with all regular messages, distillation should work"
    (d-core/run-in-gmt-tz
     (run-as-of "2012-05-12"
       (pgen/run-demarcated-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)]}
                                                :UI-IDS-ALLOWED [:FACEBOOK]
                                                :UI-IDS-COUNT 1}
                                             (let [dt (->> u t/all-threads first (t-distiller/distill u))]
                                               (has-keys dt [:thread/guid :thread/subject :thread/lm-from-contact :thread/provider :thread/messages])
                                               (has-keys (:thread/lm-from-contact dt) [:contact/first-name :contact/last-name :contact/guid :contact/muted :contact/picture-url :contact/social-identities])
                                               (doseq [m (:thread/messages dt)]
                                                 (has-keys m [:message/message-id :message/guid :message/provider :message/thread-id :message/from :message/to :message/date :message/text :message/snippet :message/sent :message/author :message/reply-to])
                                                 (has-keys (:message/author m) [:author/first-name :author/last-name :author/picture-url])
                                                 (doseq [r (:message/reply-to m)]
                                                   (has-keys r [:reply-to/first-name :reply-to/last-name :reply-to/provider-uid])))))))))