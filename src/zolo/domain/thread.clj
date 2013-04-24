(ns zolo.domain.thread
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.message :as m]
            [zolo.domain.user :as u]))

(defn messages->thread [[thread-id msgs]]
  {
   :thread/subject nil
   :thread/messages (sort-by m/message-date msgs)})

(defn messages->threads [msgs]
  (if (empty? msgs)
    []
    (->> msgs
         (group-by m/thread-id)
         (map messages->thread))))

(defn is-follow-up? [u thread]
  (let [last-m (-> thread :thread/messages last)
        m-info [(:message/provider last-m) (:message/from last-m)]
        provider-uids (u/all-user-identities-info u)]
    (some #{m-info} provider-uids)))

(def is-reply-to? (complement is-follow-up?))

(defn- filter-by-reply-to [u threads]
  (filter #(is-reply-to? u %) threads))

(defn find-reply-to-threads [u]
  (it-> u
        (m/all-messages it)
        (messages->threads it)
        (filter-by-reply-to u it)))