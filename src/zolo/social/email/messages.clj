(ns zolo.social.email.messages
    (:use zolo.utils.debug
          zolo.utils.clojure)
    (:require [zolo.social.email.gateway :as gateway]
              [zolo.utils.domain :as domain]
              [zolo.utils.calendar :as zcal]
              [zolo.utils.string :as zstring]))

(defn to [m]
  (concat
   (get-in m [:addresses :to])
   (get-in m [:addresses :bcc])))

(defn cc [m]
  (get-in m [:addresses :cc]))

(defn bcc [m]
  (get-in m [:addresses :bcc]))

(defn from [m]
  (get-in m [:addresses :from :email]))

(defn- message-body [m]
  (->> m :body (filter #(= "text/plain" (:type %))) first :content))

(defn cio-message->message [m]
  (domain/force-schema-types
   {:message/message-id (:email_message_id m)
    :message/provider :provider/email
    :message/subject (:subject m)
    :message/text (message-body m)
    :message/date (-> m :date_received zcal/seconds->instant)
    :message/from (from m)
    :message/to (->> m to (map :email))
    :message/cc (->> m cc (map :email))
    :message/thread-id (:gmail_thread_id m)
    ;; :message/reply-to
    }))

(defn missing-fields? [m]
  (some nil? [(to m) (from m) (:date_received m)]))

(defn select-valid [all-msgs]
  (remove missing-fields? all-msgs))

(defn get-messages [cio-account-id date-in-seconds]
  (it-> cio-account-id
        (gateway/get-messages it date-in-seconds)
        (select-valid it)
        (domap cio-message->message it)))

(defn get-deleted-messages [cio-account-id date-in-seconds]
  (it-> cio-account-id
        (gateway/get-deleted-messages it date-in-seconds)
        (domap cio-message->message it)))

(defn get-messages-for-thread [cio-account-id any-message-id-in-thread]
  (it-> cio-account-id
        (gateway/get-thread it any-message-id-in-thread)
        (get-in it [:body :messages])
        (select-valid it)
        (domap cio-message->message it)))

