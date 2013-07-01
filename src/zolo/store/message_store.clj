(ns zolo.store.message-store
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [zolo.demonic.core :as demonic]
            [zolo.store.user-store :as u-store]))

(defn find-entity-id-by-id [m-id]
  (when m-id
    (->> m-id
         (demonic/run-query '[:find ?m :in $ ?m-id :where [?m :message/message-id ?m-id]])
         ffirst)))

(defn find-by-id [m-id]
  (-> m-id
      find-entity-id-by-id
      demonic/load-entity))

(defn delete-temp-messages [u]
  (->> u
       :user/temp-messages
       (doeach demonic/delete))
  (u-store/reload u))

(defn append-messages [u messages]
  (demonic/append-multiple-and-reload u :user/messages messages))

(defn append-temp-message [u t-message]
  (demonic/append-single-and-reload u :user/temp-messages t-message))

(defn update-message [message]
  (demonic/insert-and-reload message))