(ns zolo.facebook.inbox
  (:use [clojure.core.match :only [match]]
        [slingshot.slingshot :only [throw+ try+]]
        zolo.facebook.gateway
        zolodeck.utils.debug
        zolodeck.utils.calendar
        zolodeck.utils.clojure))

(defn inbox-fql []
  "SELECT thread_id FROM thread WHERE folder_id = 0 ")

(defnk message-fql [:start-time BEGINNING-OF-TIME :thread-id nil]
  (if-not thread-id (throw+ {:severity :high :reason :missing-value :field :thread-id}))
  (str "SELECT message_id, thread_id, author_id, body, created_time, attachment, viewer_id FROM message WHERE thread_id = " thread-id " and created_time > " start-time))

(defn fetch-thread-ids [auth-token]
  (run-fql auth-token (inbox-fql)))

(defn fetch-inbox-threads [auth-token start-time]
  (let [thread (first (fetch-inbox-threads))]
    (run-fql auth-token (message-fql :start-time)))
)

