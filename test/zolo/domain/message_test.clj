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
          imbc (message/messages-by-contacts shy (:user/contacts shy))]

      (is (not (nil? imbc)))

      (is (= 2 (count imbc)))

      (is (= (set (:user/contacts shy)) (set (keys imbc))))

      (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))]
        (is (empty? (imbc jack)))
        (is (empty? (imbc jill))))))

  (testing "when messages are present"
    (let [vincent (vincent-persona/create-domain)
          imbc (message/messages-by-contacts vincent (:user/contacts vincent))]
      
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
          msgs (-> u
                   (interaction/ibc (:user/contacts u))
                   interaction/messages-from-ibc)]
      (is (nil? (message/last-sent-message jack msgs)))))

  (testing "When only temp message is present it should return the last temp message")

  (testing "When only regular message is present it should return the last regular message"
    (d-core/run-in-gmt-tz
      (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 5 15)]}})
            jack (first (:user/contacts u))
            msgs (-> u
                     (interaction/ibc (:user/contacts u))
                     interaction/messages-from-ibc)
            l-msg (message/last-sent-message jack msgs)]
        (is (not (nil? l-msg)))
        (is (= #inst "2012-05-14T00:02:00.000000000-00:00" (message/message-date l-msg))))))

  (testing "When temp and regular messages are present it should return the last sent message")) 

(deftest test-mark-as-done
  (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Amrut" "Indya" 1 1)]}})
        u-ui (-> u :user/user-identities first)
        m (->> u :user/messages first)
        tm (message/create-temp-message u-ui "from" ["to"] :provider/facebook "thread-id" "subject" "text")]
    (is (not (message/message-done? m)))
    (is (message/message-done? (message/set-doneness m true)))
    (is (not (message/message-done? tm)))
    (is (message/message-done? (message/set-doneness tm true)))))

(deftest test-mark-follow-up
  (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Amrut" "Indya" 1 1)]}})
        u-ui (-> u :user/user-identities first)
        m (->> u :user/messages first)
        tm (message/create-temp-message u-ui "from" ["to"] :provider/facebook "thread-id" "subject" "text")
        fud #inst"2017-07-07T17:17:17.003Z"]

    (is (not (message/follow-up-on m)))
    (is (= fud (message/follow-up-on (message/set-follow-up-on m fud))))

    (is (not (message/message-done? tm)))
    (is (thrown? RuntimeException (message/set-follow-up-on tm fud)))))
