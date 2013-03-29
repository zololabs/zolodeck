(ns zolo.store.message-store
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [zolodeck.demonic.core :as demonic]
            [zolo.store.user-store :as u-store]))

(defn delete-temp-messages [u]
  (->> u
       :user/temp-messages
       (doeach demonic/delete))
  (u-store/reload u))

(defn append-messages [u messages]
  (demonic/append-multiple-and-reload u :user/messages messages))

(defn append-temp-message [u t-message]
  (demonic/append-single-and-reload u :user/temp-messages t-message))