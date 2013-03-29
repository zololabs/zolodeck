(ns zolo.store.message-store
  (:use zolodeck.utils.debug)
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [zolodeck.demonic.core :as demonic]))


(defn delete-temp-messages [u]
  u
  ;; (->> u
  ;;      :user/temp-messages
  ;;      (doeach demonic/delete))
  )

;;TODO test
(defn append-messages [u messages]
  (demonic/append-multiple-and-reload u :user/messages messages))
