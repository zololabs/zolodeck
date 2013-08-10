(ns zolo.social.facebook.chat
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [zolo.domain.user-identity :as user-identity]
            [zolo.utils.logger :as logger])
  (:import [zolo.facebook.chat FacebookChat]))


(def CONNECTIONS (atom {}))

(defn add-to-connections! [uid chat-conn]
  (logger/info "Adding new Connection")
  (swap! CONNECTIONS assoc uid chat-conn))

(defn chat-connected? [uid]
  (if-let [conn (@CONNECTIONS uid)]
    (.isLive conn)))

(defn connect-user! [uid access-token]
  (when-not (chat-connected? uid)
    (add-to-connections! uid (FacebookChat. (conf/fb-app-id) access-token))))

(defn jid-for [uid]
  (str "-" uid "@chat.facebook.com"))

;; TODO - enable mutli user chat here
(defn send-message [fb-id access-token to-uids message]
  (connect-user! fb-id access-token)
  (doseq [to-uid to-uids]
    (.sendMessage (@CONNECTIONS fb-id) (jid-for to-uid) message)))
