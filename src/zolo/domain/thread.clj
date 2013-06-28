(ns zolo.domain.thread
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.message :as m]
            [zolo.domain.user :as u]
            [zolo.domain.contact :as c]
            [clojure.string :as str]))

(defn- last-message [thread]
  (-> thread :thread/messages first))

(defn- lm-contact [u last-message selector-fn]
  (it-> last-message
        (c/find-by-provider-and-provider-uid u (:message/provider it) (selector-fn it))
        (c/distill-basic it)))

(defn- lm-from-to [u last-m]
  (if (:message/sent last-m)
    (lm-contact u last-m #(first (:message/to %)))
    (lm-contact u last-m :message/from)))

(defn- person-name [p]
  (str (:reply-to/first-name p) " " (:reply-to/last-name p)))

(defn- subject-from-people [distilled-message]
  (it-> distilled-message
        (:message/reply-to it)
        (map person-name it)
        (str/join ", " it)
        (str "Conversation with " it)))

(defn distill [u thread]
  (when thread
    (let [distilled-msgs (domap #(m/distill u (u/tz-offset-minutes) %) (:thread/messages thread))]
      {:thread/guid (-> distilled-msgs last :message/message-id)
       :thread/subject (or (:thread/subject thread)
                           (-> distilled-msgs first subject-from-people))
       :thread/lm-from-contact (lm-from-to u (first distilled-msgs))
       :thread/provider (-> thread :thread/messages first :message/provider)
       :thread/messages distilled-msgs})))

(defn distill-by-contacts [u threads]
  (group-by :thread/lm-from-contact (domap #(distill u %) threads)))

(defn- messages->thread [[thread-id msgs]]
  {:thread/guid thread-id
   :thread/subject (-> msgs first :message/subject)
   :thread/messages (reverse-sort-by m/message-date msgs)})

(defn contact-exists? [u thread selector-fn]
  (let [last-m (last-message thread)]
    (->> last-m
         selector-fn
         (c/find-by-provider-and-provider-uid u (:message/provider last-m))
         c/is-a-person?)))

(defn reply-to-contact-exists? [u thread]
  (contact-exists? u thread :message/from))

;; TODO - this needs to check for each contact once group chat is supported
(defn follow-up-contact-exists? [u thread]
  (contact-exists? u thread #(first (:message/to %))))

;; TODO - filtering group-chats is temporary, remove this once we support reply-to-multiple
(defn messages->threads [u msgs]
  (if (empty? msgs)
    []
    (->> msgs
         (group-by m/thread-id)
         (map messages->thread))))

(defn- is-follow-up? [u thread]
  (let [last-m (-> thread :thread/messages first)
        m-info [(:message/provider last-m) (:message/from last-m)]
        provider-uids (u/all-user-identities-info u)]
    (some #{m-info} provider-uids)))

(def ^:private is-reply-to? (complement is-follow-up?))

(defn- sort-by-recent-threads [threads]
  (reverse-sort-by #(-> % :thread/messages first m/message-date) threads))

(defn all-threads [u]
  (it-> u
        (m/all-messages it)
        (messages->threads u it)))

(defn find-reply-to-threads [u]
  (it-> u
        (all-threads it)
        
        (filter #(is-reply-to? u %) it)
        (filter #(reply-to-contact-exists? u %) it)
        
        (sort-by-recent-threads it)))

(defn find-follow-up-threads [u]
  (it-> u
        (all-threads it)
        (filter #(is-follow-up? u %) it)
        (filter #(follow-up-contact-exists? u %) it)
        (sort-by-recent-threads it)))

