(ns zolo.utils.domain
  (:require [zolodeck.utils.maps :as maps]
            [zolodeck.utils.calendar :as calendar]
            [zolodeck.demonic.schema :as schema]))

(def FB-USER-KEYS 
     {:first_name :user/first-name
      :last_name :user/last-name
      :gender :user/gender
      :link :user/fb-link
      :username :user/fb-username
      :email :user/fb-email
      :id :user/fb-id
      :auth-token :user/fb-auth-token})

(def FB-FRIEND-KEYS
    {:first_name :contact/first-name
     :last_name :contact/last-name
     :gender :contact/gender

     :id :contact/fb-id
     :link :contact/fb-link
     :birthday :contact/fb-birthday
     :picture :contact/fb-picture-link})

(def FB-MESSAGE-KEYS
    {:attachment :message/attachments
     :author_id :message/from
     :body :message/text
     :created_time :message/date
     :message_id :message/message-id
     :thread_id :message/thread-id
     :viewer_id :message/to})

(defn force-schema-attrib [attrib value]
  (cond
   (and (schema/is-string? attrib) (not (string? value))) (str value)
   :else value))

(defn force-schema-types [a-map]
  (maps/transform-vals-with a-map force-schema-attrib))

(defn group-by-attrib [objects attrib]
  (-> (group-by attrib objects)
      (maps/transform-vals-with (fn [_ v] (first v)))))

(defn fb-user->user [fb-user]
  (maps/update-all-map-keys fb-user FB-USER-KEYS))

(defn fb-friend->contact [fb-friend]
  (-> fb-friend
      (assoc :birthday (calendar/date-string->instant "MM/dd/yyyy" (:birthday fb-friend)))
      (maps/update-all-map-keys FB-FRIEND-KEYS)))

(defn fb-message->message [fb-message]
  (-> fb-message
      (assoc :created_time (calendar/millis->instant (-> fb-message :created_time (* 1000))))
      (maps/update-all-map-keys FB-MESSAGE-KEYS)
      (force-schema-types)))