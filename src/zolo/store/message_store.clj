(ns zolo.store.message-store
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [zolo.demonic.core :as demonic]
            [zolo.demonic.helper :as dh]
            [zolo.store.user-store :as u-store]))

(defn find-entity-id-by-ui-guid-and-id [ui-guid m-id]
  (when (and ui-guid m-id)
    (-> (demonic/run-query '[:find ?m :in $ ?ui-guid ?m-id
                             :where
                             [?m :message/message-id ?m-id]
                             [?m :message/user-identity ?ui]
                             [?ui :identity/guid ?ui-guid]]
                           (to-uuid ui-guid) m-id)
        ffirst)))

(defn find-by-ui-guid-and-id [ui-guid m-id]
  (->> m-id
       (find-entity-id-by-ui-guid-and-id ui-guid)
       demonic/load-entity))

(defn find-entity-by-ui-guid-and-id [ui-guid m-id]
  (->> m-id
       (find-entity-id-by-ui-guid-and-id ui-guid)
       dh/load-from-db))

(defn find-message-ids-by-ui-guid-and-thread-guid [ui-guid thread-id]
  (->> (demonic/run-query '[:find ?m :in $ ?ui-guid ?t-id
                           :where
                            [?m :message/thread-id ?t-id]
                            [?m :message/user-identity ?ui]
                            [?ui :identity/guid ?ui-guid]]
                          (to-uuid ui-guid) thread-id)
       (map first)))

(defn find-messages-by-ui-guid-and-thread-guid [ui-guid thread-id]
  (->> thread-id
       (find-message-ids-by-ui-guid-and-thread-guid ui-guid)
       (map demonic/load-entity)))

(defn find-message-entities-by-ui-guid-and-thread-guid [ui-guid thread-id]
  (->> thread-id
       (find-message-ids-by-ui-guid-and-thread-guid ui-guid)
       (map dh/load-from-db)))

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