(ns zolo.facebook.inbox
  (:require [zolo.utils.http :as gigya-gateway])
  (:use [slingshot.slingshot :only [throw+ try+]]
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

(defn gigya-fetch-thread [u thread-info start-time]
  (let [{thread-id :thread_id recipients :recipients subject :subject} thread-info]
    (print-vals "Thread-id:" thread-id)
    (->> (gigya-gateway/gigya-raw-data-post {"provider" "facebook"
                                             "UID" (:user/guid u)
                                             "fields" (message-fql thread-id start-time)})
         (expand-messages subject recipients))))

(defn gigya-fetch-threads [u]
  (gigya-gateway/gigya-raw-data-post {"provider" "facebook"
                                      "UID" (:user/guid u)
                                      "fields" INBOX-FQL}))

(defn get-facebook-messages [u]
  (->> (gigya-fetch-threads u)
       (mapcat #(gigya-fetch-thread u % (to-seconds "1990-01-01")))))