(ns zolo.social.facebook.messages
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.social.facebook.gateway
        zolo.utils.debug
        zolo.utils.calendar
        zolo.utils.clojure)
  (:require [clojure.data.json :as json]
            [zolo.utils.domain :as domain]
            [zolo.utils.maps :as maps]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.stream :as stream]
            [zolo.utils.calendar :as zolo-cal]
            [clj-time.coerce :as ctc]))

;;TODO this domain is still talking with Social Layer

(def FB-MESSAGE-KEYS
  {:attachment :message/attachments
   :provider :message/provider
   :author_id :message/from
   :body :message/text
   :created_time :message/date
   :message_id :message/message-id
   :thread_id :message/thread-id
   :to :message/to
   :subject :message/subject
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
})

(defn fb-message->message [fb-message]
  (-> fb-message
      (assoc :created_time (zolo-cal/millis->instant (-> fb-message :created_time (* 1000))))
      ;;TODO Make this an enum too
      (maps/update-all-map-keys FB-MESSAGE-KEYS)
      (assoc :message/provider :provider/facebook)
      (domain/force-schema-types)))

(defn fb-post->message [fb-post]
  (-> fb-post
      (assoc :created_time (ctc/to-date (:created_time fb-post)))
      (assoc :from (get-in fb-post [:from :id]))
      (assoc :to (map :id (get-in fb-post [:to :data])))
      (maps/update-all-map-keys FB-POST-KEYS)
      (assoc :message/provider :provider/facebook)
      (domain/force-schema-types)))

(def INBOX-FQL "SELECT thread_id, recipients , subject  FROM thread WHERE folder_id = 0 ")
 
(defn message-fql [thread-id start-unix-time-stamp]
  ;;TODO For now Removing attachments
    ;;  (str "SELECT message_id, thread_id, author_id, body, created_time, attachment, viewer_id FROM message WHERE thread_id = " thread-id " and created_time > " start-time)
    (str "SELECT message_id, thread_id, author_id, body, created_time FROM message WHERE thread_id = " thread-id " and created_time > " start-unix-time-stamp))

(defn except-self [recipients {author-id :author_id}]
  (remove #(= author-id %) recipients))

(defn associate-fields [subject recipients msg]
  (let [actual-recipients (except-self recipients msg)]
    (-> msg
        (assoc :subject subject)
        (assoc :to actual-recipients))))

(defn tag-message-fields [subject recipients msgs]
  (map #(associate-fields subject recipients %) msgs))
 
(defn- messages-fql-for-thread [auth-token thread-info start-unix-time-stamp]
  (let [{thread-id :thread_id recipients :recipients subject :subject} thread-info]
    [thread-id (message-fql thread-id start-unix-time-stamp)]))

(defn fetch-threads-info [auth-token]
  (->> INBOX-FQL
       (run-fql auth-token)
       (maps/group-first-by :thread_id)))

(defn- process-thread-result [threads-info thread-id messages]
  (tag-message-fields (get-in threads-info [thread-id :subject])
                      (get-in threads-info [thread-id :recipients])
                      messages))

(defn fetch-inbox [auth-token start-unix-time-stamp]
  (let [threads-info (fetch-threads-info auth-token)]
    (->> (vals threads-info)
         (mapcat #(messages-fql-for-thread auth-token % start-unix-time-stamp))
         (apply hash-map)
         (process-fql-multi auth-token #(process-thread-result threads-info %1 %2)))))

(defn fetch-feed [auth-token user-id yyyy-MM-dd-string]
  ;(logger/trace "Fetching feed for user-id:" user-id ", from:" yyyy-MM-dd-string)
  (domap fb-post->message (stream/recent-activity auth-token user-id yyyy-MM-dd-string)))

(defn fetch-messages [auth-token user-id date]
  (domap fb-message->message (fetch-inbox auth-token date)))

(defn fetch-all-contact-feeds [access-token last-updated-string provider-uids]
  (->> provider-uids
       (stream/fetch-contact-feeds access-token last-updated-string)
       (domap fb-post->message)))