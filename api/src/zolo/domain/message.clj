(ns zolo.domain.message
  (:use zolodeck.utils.debug)
  (:require [clojure.set :as set]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolo.domain.contact :as contact]
            [zolo.domain.social-identity :as social-identity]
            [zolo.facebook.inbox :as fb-inbox]
            [zolodeck.demonic.schema :as schema]
            [zolodeck.demonic.core :as demonic]))

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
      (assoc :mode "Inbox-Message")
      (zolo-maps/update-all-map-keys FB-MESSAGE-KEYS)
      (utils-domain/force-schema-types)))

(defn group-by-contact-fb-id [user messages]
  (let [grouped-by-from (group-by :message/from messages)
        grouped-by-to (group-by :message/to messages)
        grouped  (merge-with concat grouped-by-from grouped-by-to)]
    (dissoc grouped (:user/fb-id user))))

;;TODO Need to find a better place for this function
(defn user-provider-infos [user]
  (->> user
      :user/social-identities
      (map social-identity/social-identity-info)))

(defn dissoc-user-messages [user grouped-messages]
  (reduce (fn [msgs user-provider-info]
            (dissoc msgs user-provider-info))
          grouped-messages
          (user-provider-infos user)))

;;TODO Duplication
(defn message-from-provider-info [m]
  [(:message/provider m) (:message/from m)])

(defn message-to-provider-info [m]
  [(:message/provider m) (:message/to m)])

(defn group-by-provider-info [user messages]
  (let [grouped-by-from (group-by message-from-provider-info messages)
        grouped-by-to (group-by message-to-provider-info messages)
        grouped  (merge-with concat grouped-by-from grouped-by-to)]
    (dissoc-user-messages user grouped)))

(defn process-contact-messages [user provider-info fresh-messages]
  (let [contact (or (contact/find-contact-by-provider-info user provider-info)
                    (contact/create-contact user provider-info))]
    (assoc contact :contact/messages
           (utils-domain/update-fresh-entities-with-db-id (:contact/messages contact) fresh-messages :message/message-id :message/guid))))

(defn merge-messages [user fresh-messages]
  (let [grouped (group-by-provider-info user fresh-messages)]
    (map (fn [[provider-info msgs]]
           (process-contact-messages user provider-info msgs)) grouped)))

(defn update-messages [user]
  (->> (fb-inbox/get-facebook-messages user)
       (map fb-message->message)
       (merge-messages user)
       (map demonic/insert)
       doall))

