(ns zolo.domain.thread-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
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
            [zolo.test.assertions.domain :as d-assert]))

(deftest test-find-reply-to-threads

  (testing "When user does not have any messages it should return empty"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)]}})]
      (is (empty? (t/find-reply-to-threads u)))))

  (testing "When user has 1 thread with last message sent ... it should return empty"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 8)]}})]
      (is (empty? (t/find-reply-to-threads u)))))

  (testing "When user has 1 thread with last message received ... it should return that thread"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 9)]}})
          threads (t/find-reply-to-threads u)]

      (is (= 1 (count threads)))
      (is (= (set (m/all-messages u)) (set (:thread/messages (first threads))))))))