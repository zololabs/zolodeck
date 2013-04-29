(ns zolo.domain.thread
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.message :as m]
            [zolo.domain.user :as u]
            [zolo.domain.contact :as c]
            [clojure.string :as str]))

(defn- lm-from-contact [u thread]
  (it-> thread
        (:thread/messages it)
        (first it)
        (c/find-by-provider-and-provider-uid u (:message/provider it) (:message/from it))
        (c/distill-basic it)))

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
    (let [distilled-msgs (map #(m/distill u %) (:thread/messages thread))]
      {:thread/guid (:thread/guid thread)
       :thread/subject (or (:thread/subject thread)
                           (-> distilled-msgs first subject-from-people))
       :thread/lm-from-contact (lm-from-contact u thread)
       :thread/provider (-> thread :thread/messages first :message/provider)
       :thread/messages distilled-msgs})))

(defn- messages->thread [[thread-id msgs]]
  {:thread/guid thread-id
   :thread/subject (-> msgs first :message/subject)
   :thread/messages (reverse-sort-by m/message-date msgs)})

(defn messages->threads [msgs]
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

(defn- filter-by-reply-to [u threads]
  (filter #(is-reply-to? u %) threads))

(defn find-reply-to-threads [u]
  (it-> u
        (m/all-messages it)
        (messages->threads it)
        (filter-by-reply-to u it)))

(defn find-follow-up-threads [u])