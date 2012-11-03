(ns zolo.social.facebook.messages
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.social.facebook.gateway
        zolodeck.utils.debug
        zolodeck.utils.calendar
        zolodeck.utils.clojure)
  (:require [zolo.utils.domain :as domain]
            [zolodeck.utils.maps :as maps]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.stream :as stream]
            [zolodeck.utils.calendar :as zolo-cal]
            [clj-time.coerce :as ctc]))

(def MODE-INBOX "INBOX")
(def MODE-FEED "FEED")

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

(def FB-POST-KEYS
  {:id :message/message-id
   :from :message/from
   :created_time :message/date
   :message :message/text
   :story :message/story
   :to :message/to
   :picture :message/picture 
   :link :message/link
   :icon :message/icon
   :mode :message/mode
})

(defn fb-message->message [fb-message]
  (-> fb-message
      (assoc :created_time (zolo-cal/millis->instant (-> fb-message :created_time (* 1000))))
      ;;TODO Make this an enum too
      (assoc :mode MODE-INBOX)
      (maps/update-all-map-keys FB-MESSAGE-KEYS)
      (assoc :message/provider :provider/facebook)
      (domain/force-schema-types)))

(defn fb-post->message [fb-post]
  (-> fb-post
      (assoc :created_time (ctc/to-date (:created_time fb-post)))
      (assoc :from (get-in fb-post [:from :id]))
      (assoc :to (map :id (get-in fb-post [:to :data])))
      (assoc :mode MODE-FEED)
      (maps/update-all-map-keys FB-POST-KEYS)
      (assoc :message/provider :provider/facebook)
      (domain/force-schema-types)))

(def INBOX-FQL "SELECT thread_id, recipients , subject  FROM thread WHERE folder_id = 0 ")
 
(defn message-fql [thread-id start-time]
  ;;TODO For now Removing attachments
    ;;  (str "SELECT message_id, thread_id, author_id, body, created_time, attachment, viewer_id FROM message WHERE thread_id = " thread-id " and created_time > " start-time)
    (str "SELECT message_id, thread_id, author_id, body, created_time FROM message WHERE thread_id = " thread-id " and created_time > " start-time))

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
     (logger/trace "Fetching messages from:" start-date-yyyy-MM-dd-string)
     (->> INBOX-FQL
          (run-fql auth-token)
          (mapcat #(fetch-thread auth-token % (to-seconds start-date-yyyy-MM-dd-string)))
          (map fb-message->message)))
  ([auth-token]
     (fetch-inbox auth-token "1990-01-01")))

;; TODO this date needs to be based on last refreshed data
(defn fetch-feed [auth-token user-id yyyy-MM-dd-string]
  (logger/trace "Fetching feed for user-id:" user-id ", from:" yyyy-MM-dd-string)
  (->> (stream/recent-activity-until auth-token user-id yyyy-MM-dd-string)
       (map fb-post->message)))

(defn fetch-all-messages [auth-token user-id date]
  (concat (fetch-inbox auth-token date)
          (fetch-feed auth-token user-id date)))