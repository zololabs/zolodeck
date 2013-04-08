(ns zolo.social.email.messages
    (:use zolo.utils.debug
          zolo.utils.clojure)
    (:require [zolo.social.email.gateway :as gateway]
              [zolo.utils.domain :as domain]
              [zolo.utils.calendar :as zcal]))

(defn to [m]
  (get-in m [:addresses :to]))

(defn from [m]
  (get-in m [:addresses :from :email]))

(defn cio-message->message [m]
  (domain/force-schema-types
   {:message/message-id (:email_message_id m)
    :message/provider :provider/email
    :message/subject (:subject m)
    ;; :message/text
    :message/date (-> m :date_received zcal/seconds->instant)
    :message/from (from m)
    :message/to (->> m to (map :email))
    :message/thread-id (:gmail_thread_id m)
    ;; :message/reply-to
    }))

(defn missing-fields? [m]
  (some nil? [(to m) (from m) (:date_received m)]))

(defn select-valid [all-msgs]
  (remove missing-fields? all-msgs))

(defn get-messages [cio-account-id]
  (->> cio-account-id
       gateway/get-messages
       select-valid
       (domap cio-message->message)))