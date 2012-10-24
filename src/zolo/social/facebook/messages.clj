(ns zolo.social.facebook.messages
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.social.facebook.gateway
        zolodeck.utils.debug
        zolodeck.utils.calendar
        zolodeck.utils.clojure)
  (:require [zolo.utils.domain :as domain]
            [zolodeck.utils.maps :as maps]
            [zolodeck.utils.calendar :as zolo-cal]))

(def MODE-INBOX "INBOX")

(def FB-MESSAGE-KEYS
  {:attachment :message/attachments
   :provider :message/provider
   :mode :message/mode
   :author_id :message/from
   :body :message/text
   :created_time :message/date
   :message_id :message/message-id
   :thread_id :message/thread-id
   :to :message/to
   ;;TODO Add :message/subject
   })

(defn fb-message->message [fb-message]
  (-> fb-message
      (assoc :created_time (zolo-cal/millis->instant (-> fb-message :created_time (* 1000))))
      ;;TODO Make this an enum too
      (assoc :mode MODE-INBOX)
      (maps/update-all-map-keys FB-MESSAGE-KEYS)
      (assoc :message/provider :provider/facebook)
      (domain/force-schema-types)))

(def INBOX-FQL "SELECT thread_id, recipients , subject  FROM thread WHERE folder_id = 0 ")
 
(defn message-fql [thread-id start-time]
  ;;TODO For now Removing attachments
    ;;  (str "SELECT message_id, thread_id, author_id, body, created_time, attachment, viewer_id FROM message WHERE thread_id = " thread-id " and created_time > " start-time)
    (str "SELECT message_id, thread_id, author_id, body, created_time FROM message WHERE thread_id = " thread-id " and created_time > " start-time))
 
;; (defn update-message [subject recipient msg]
;;   (when-not (= recipient (:author_id msg))
;;     (-> msg
;;         (assoc :subject subject)
;;         (assoc :to recipient))))
 
;; (defn duplicate-msg-for-each-recipient [subject recipients msg]
;;   (keep #(update-message subject % msg) recipients))

(defn except-self [recipients {author-id :author_id}]
  (remove #(= author-id %) recipients))

(defn associate-fields [subject recipients msg]
  (let [actual-recipients (except-self recipients msg)]
    (-> msg
        (assoc :subject subject)
        (assoc :to actual-recipients))))

(defn expand-messages [subject recipients msgs]
  ;(mapcat #(duplicate-msg-for-each-recipient subject recipients %) msgs)
  (map #(associate-fields subject recipients %) msgs)
  )
 
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
          (mapcat #(fetch-thread auth-token % (to-seconds start-date-yyyy-MM-dd-string)))
          (map fb-message->message)))
  ([auth-token]
     (fetch-inbox auth-token "1990-01-01")))