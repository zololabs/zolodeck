(ns zolo.domain.message
  (:use zolodeck.utils.debug)
  (:require [clojure.set :as set]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolo.domain.contact :as contact]
            [zolodeck.demonic.schema :as schema]))

;;TODO No test present for this namespace :(

(def FB-MESSAGE-KEYS
    {:attachment :message/attachments
     :author_id :message/from
     :body :message/text
     :created_time :message/date
     :message_id :message/message-id
     :thread_id :message/thread-id
     :viewer_id :message/to})

(defn fb-message->message [fb-message]
  (-> fb-message
      (assoc :created_time (zolo-cal/millis->instant (-> fb-message :created_time (* 1000))))
      (zolo-maps/update-all-map-keys FB-MESSAGE-KEYS)
      (utils-domain/force-schema-types)))

(defn group-by-contact-fb-id [user messages]
  (let [grouped-by-from (group-by :message/from messages)
        grouped-by-to (group-by :message/to messages)
        grouped (merge-with concat grouped-by-from grouped-by-to)]
    (dissoc  grouped (:user/fb-id user))))

;; TODO - refactor this (and update-fresh-contacts-with-db-id) into
;; domain utils
(defn update-fresh-messages-with-db-id [existing-messages fresh-messages]
  (let [existing-messages-grouped (utils-domain/group-by-attrib existing-messages :message/message-id)
        fresh-messages-grouped (utils-domain/group-by-attrib fresh-messages :message/message-id)]
    (map
     (fn [[m-id fresh-message]]
       (assoc fresh-message :db/id (:db/id (existing-messages-grouped m-id))))
     fresh-messages-grouped)))

(defn process-contact-messages [user contact-fb-id fresh-messages]
  (let [contact (contact/find-by-user-and-contact-fb-id user contact-fb-id)
        existing-messages (:contact/messages contact)
        updated-fresh-messages (if (empty? existing-messages)
                                 fresh-messages
                                 (update-fresh-messages-with-db-id existing-messages fresh-messages))]
    (assoc contact :contact/messages updated-fresh-messages)))

(defn merge-messages [user fresh-messages]
  (let [grouped (group-by-contact-fb-id user fresh-messages)]
    (map (fn [[c-id msgs]] (process-contact-messages user c-id msgs)) grouped)))

