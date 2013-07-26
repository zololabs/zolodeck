(ns zolo.domain.thread
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.message :as m]
            [zolo.domain.user :as u]
            [zolo.domain.contact :as c]
            [clojure.string :as str]
            [zolo.utils.calendar :as zcal]))

(defn earliest-message [thread]
  (-> thread :thread/messages last))

(defn last-message [thread]
  (-> thread :thread/messages first))

(defn most-recent-message-date-from-thread [thread]
  (-> thread :thread/messages first m/message-date))

(defn most-recent-message-date-from-threads [threads]
  (-> threads first most-recent-message-date-from-thread))

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

(defn sort-by-recent-threads [threads]
  (reverse-sort-by #(-> % :thread/messages first m/message-date) threads))

;; TODO - filtering group-chats is temporary, remove this once we support reply-to-multiple
(defn messages->threads [msgs]
  (if (empty? msgs)
    []
    (->> msgs
         (group-by m/thread-id)
         (map messages->thread)
         sort-by-recent-threads)))

(defn- is-follow-up-candidate? [u thread]
  (let [last-m (-> thread :thread/messages first)
        m-info [(:message/provider last-m) (:message/from last-m)]
        provider-uids (u/all-user-identities-info u)]
    (some #{m-info} provider-uids)))

(def ^:private is-reply-to? (complement is-follow-up-candidate?))

(defn last-sent-message-time [u thread]
  (->> thread :thread/messages (filter #(m/is-sent-by-user? u %)) first m/message-date))

(defn done-updated [thread]
  (-> thread earliest-message :message/done-updated))

(defn is-done? [u thread]
  (if-let [done-updated-inst (done-updated thread)]
    (when (.before (last-sent-message-time u thread) done-updated-inst)
      (-> thread earliest-message m/message-done?))))

(defn follow-up-updated [thread]
  (-> thread earliest-message :message/follow-up-updated))

(defn follow-up-on [u thread]
  (if-let [follow-up-updated-inst (follow-up-updated thread)]
    (when (.before (last-sent-message-time u thread) follow-up-updated-inst)
      (-> thread earliest-message :message/follow-up-on))))

(defn- is-last-sent-before-48-hours? [u thread]
  (-> u
      (last-sent-message-time thread)
      (zcal/plus 48 :hours)
      zcal/to-inst
      (.before (zcal/now-instant))))

(defn- is-after-follow-up-on-time? [u thread]
  (let [follow-up-on-inst (follow-up-on u thread)]
    (if follow-up-on-inst
      (.after (zcal/now-instant) follow-up-on-inst)
      (is-last-sent-before-48-hours? u thread))))

(defn is-follow-up? [u thread]
  (and (is-follow-up-candidate? u thread)
       (is-after-follow-up-on-time? u thread)))

(defn all-threads [u thread-limit thread-offset]
  (it-> u
        (m/all-messages it)
        (reverse-sort-by m/message-date it)
        (messages->threads it)
        (drop thread-offset it)
        (take thread-limit it)))

(defn find-reply-to-threads [u thread-limit thread-offset]
  (it-> u
        (all-threads it thread-limit thread-offset)        
        (filter #(is-reply-to? u %) it)
        (filter #(reply-to-contact-exists? u %) it)
        (remove #(is-done? u %) it)
        (sort-by-recent-threads it)))

(defn find-follow-up-threads [u thread-limit thread-offset]
  (it-> u
        (all-threads it thread-limit thread-offset)
        (filter #(is-follow-up? u %) it)
        (filter #(follow-up-contact-exists? u %) it)
        (remove #(is-done? u %)  it)
        (sort-by-recent-threads it)))

