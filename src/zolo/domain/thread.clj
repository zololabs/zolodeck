(ns zolo.domain.thread
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.message :as m]
            [zolo.domain.user :as u]
            [zolo.domain.contact :as c]))

(defn distill [u thread]
  (when thread
    {:thread/guid (:thread/guid thread)
     :thread/subject (:thread/subject thread)
     :thread/lm-from-contact (it-> thread
                                   (:thread/messages it)
                                   (last it)
                                   (c/find-by-provider-and-provider-uid u (:message/provider it) (:message/from it))
                                   (c/distill-basic it))
;     :thread/lm-to-contacts 
     :thread/messages (map m/distill (:thread/messages thread))}))

(defn messages->thread [[thread-id msgs]]
  {:thread/guid thread-id
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

(defn find-follow-up-threads [u])