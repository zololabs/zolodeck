(ns zolo.social.facebook.chat
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as conf]
            [zolo.domain.user-identity :as user-identity]
            [zolo.utils.logger :as logger])
  (:import [zolo.facebook.chat FacebookChat]))


(def CONNECTIONS (atom {}))

(defn add-to-connections! [uid chat-conn]
  (swap! CONNECTIONS assoc uid chat-conn))

(defn connect-user! [u]
  (let [{uid :social/provider-uid access-token :social/auth-token} (user-identity/fb-user-identity u)]
    (logger/trace "UID:" uid)
    (logger/trace "AT:" access-token)    
    (add-to-connections! uid (FacebookChat. (conf/fb-app-id) access-token))))

(defn jid-for [uid]
  (str "-" uid "@chat.facebook.com"))

(defn send-message [from-uid to-uid message]
  (let [chat (@CONNECTIONS from-uid)]
    (.sendMessage chat (jid-for to-uid) message)))
