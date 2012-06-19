(ns zolo.domain.message
  (:use zolodeck.utils.debug)
  (:require [clojure.set :as set]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
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

(defn merge-messages [user fresh-messages]
  (let [existing-messages-grouped (utils-domain/group-by-attrib (:user/messages user) :message/message-id)
        fresh-messages-grouped (utils-domain/group-by-attrib fresh-messages :message/message-id)
        new-message-ids  (set/difference (-> fresh-messages-grouped keys set)
                                         (-> existing-messages-grouped keys set))
        added-messages (map fresh-messages-grouped new-message-ids)]
    (assoc user :user/messages (concat added-messages (vals existing-messages-grouped)))))

