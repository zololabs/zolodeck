(ns zolo.social.facebook.messages
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.social.facebook.gateway
        zolodeck.utils.debug
        zolodeck.utils.calendar
        zolodeck.utils.clojure))
 
(def INBOX-FQL "SELECT thread_id, recipients , subject  FROM thread WHERE folder_id = 0 ")
 
(defn message-fql [thread-id start-time]
  ;;TODO For now Removing attachments
    ;;  (str "SELECT message_id, thread_id, author_id, body, created_time, attachment, viewer_id FROM message WHERE thread_id = " thread-id " and created_time > " start-time)
    (str "SELECT message_id, thread_id, author_id, body, created_time FROM message WHERE thread_id = " thread-id " and created_time > " start-time))
 
(defn update-message [subject recipient msg]
  (when-not (= recipient (:author_id msg))
    (-> msg
        (assoc :subject subject)
        (assoc :to recipient))))
 
(defn duplicate-msg-for-each-recipient [subject recipients msg]
  (keep #(update-message subject % msg) recipients))
 
(defn expand-messages [subject recipients msgs]
  (mapcat #(duplicate-msg-for-each-recipient subject recipients %) msgs))
 
(defn fetch-thread [auth-token thread-info start-time]
  (let [{thread-id :thread_id recipients :recipients subject :subject} thread-info]
    (->> (message-fql thread-id start-time)
         (run-fql auth-token)
         (expand-messages subject recipients))))
 
;;TODO Random date is passed ... need to fix this
(defn fetch-inbox
  ([auth-token start-date-yyyy-MM-dd-string]
     (->> INBOX-FQL
          (run-fql auth-token)
          (mapcat #(fetch-thread auth-token % (to-seconds start-date-yyyy-MM-dd-string)))))
  ([auth-token]
     (fetch-inbox auth-token "1990-01-01")))