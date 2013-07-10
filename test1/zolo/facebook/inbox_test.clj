(ns zolo.facebook.inbox-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.scenario
        zolo.test.core-utils
        zolo.test.web-utils
        zolo.utils.debug
        zolo.utils.test
        zolo.utils.test-utils
        zolo.utils.http-status-codes
        zolo.marconi.facebook.core)
  (:require [zolo.facebook.inbox :as inbox]
            [zolo.api.user-api :as user-api]))

(deftest ^:integration test-fetch-inbox-threads
  (let [inbox (inbox/fetch-inbox (hobbes-access-token) "2012-05-01")]
    (is (= 6 (count inbox)))
    (doseq [m inbox]
      (is-same-sequence?
       ;;TODO When Attachment is added back uncomment this line
;;       [:message_id :thread_id :author_id :body :created_time :attachment :viewer_id]
       [:message_id :thread_id :author_id :body :created_time :subject :to]
       (keys m)))))



