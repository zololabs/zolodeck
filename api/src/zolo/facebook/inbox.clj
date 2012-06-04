(ns zolo.facebook.inbox
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.facebook.gateway
        zolodeck.utils.debug
        zolodeck.utils.clojure))

(def INBOX-FQL "SELECT thread_id FROM thread WHERE folder_id = 0 ")

(defn message-fql [thread-id start-time]
  (str "SELECT message_id, thread_id, author_id, body, created_time, attachment, viewer_id FROM message WHERE thread_id = " thread-id " and created_time > " start-time))

(defnk fetch-thread [auth-token thread-id start-time]
  (->> (message-fql thread-id start-time)
       (run-fql auth-token)))

(defn fetch-inbox [auth-token start-time]
  (->> INBOX-FQL
       (run-fql auth-token)
       (mapcat #(fetch-thread auth-token % start-time))))

