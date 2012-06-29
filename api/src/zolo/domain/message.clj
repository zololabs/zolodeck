(ns zolo.domain.message
  (:use zolodeck.utils.debug)
  (:require [clojure.set :as set]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolo.domain.contact :as contact]
            [zolodeck.demonic.schema :as schema]))

(def FB-MESSAGE-KEYS
  {:attachment :message/attachments
   :platform :message/platform
   :mode :message/mode
   :author_id :message/from
   :body :message/text
   :created_time :message/date
   :message_id :message/message-id
   :thread_id :message/thread-id
   :to :message/to
   ;;TODO Add :message/subject
   })

(def ZG-MESSAGE-KEYS
  {:message/guid  :guid
   :message/message-id :message-id
   :message/platform :platform
   :message/mode :mode
   :message/text :text
   :message/date :date
   :message/from :from
   :message/to :to
   :message/thread-id :thread-id
   :message/reply-to :reply-to})

(defn fb-message->message [fb-message]
  (-> fb-message
      (assoc :created_time (zolo-cal/millis->instant (-> fb-message :created_time (* 1000))))
      (assoc :platform "Facebook")
      (assoc :mode "Inbox-Message")
      (zolo-maps/update-all-map-keys FB-MESSAGE-KEYS)
      (utils-domain/force-schema-types)))

(defn group-by-contact-fb-id [user messages]
  (let [grouped-by-from (group-by :message/from messages)
        grouped-by-to (group-by :message/to messages)
        grouped  (merge-with concat grouped-by-from grouped-by-to)]
    (dissoc grouped (:user/fb-id user))))

(defn process-contact-messages [user contact-fb-id fresh-messages]
  (let [contact (or (contact/find-by-user-and-contact-fb-id user contact-fb-id)
                    (contact/create-contact user {:contact/fb-id contact-fb-id}))]
    (assoc contact :contact/messages
           (utils-domain/update-fresh-entities-with-db-id (:contact/messages contact) fresh-messages :message/message-id))))

(defn merge-messages [user fresh-messages]
  (let [grouped (group-by-contact-fb-id user  fresh-messages)]
    (map (fn [[c-id msgs]]
           (process-contact-messages user c-id msgs)) grouped)))

