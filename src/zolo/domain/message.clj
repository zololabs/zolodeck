(ns zolo.domain.message
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [clojure.set :as set]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolo.domain.contact :as contact]
            [zolo.domain.social-identity :as social-identity]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.core :as social]            
            [zolodeck.demonic.schema :as schema]
            [zolodeck.demonic.core :as demonic]
            [zolo.utils.logger :as logger]))

(def MESSAGES-START-TIME "2000-10-22")
(def FEEDS-START-TIME "2012-10-22")

(defn message-identifier [m]
  [(:message/provider m) (:message/message-id m)])

(defn refreshed-messages [user fresh-messages]
  (utils-domain/update-fresh-entities-with-db-id (:user/messages user)
                                                 fresh-messages
                                                 message-identifier
                                                 :message/guid))

(defn get-messages-for-user-identity [user-identity last-updated-string]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-messages provider access-token provider-uid last-updated-string)))

(defn update-contact-feed [user provider contact-provider-uid user-access-token]
  (social/fetch-feed provider user-access-token contact-provider-uid))

(defn get-messages-for-user [user]
  (let [last-updated-string (or (-> user :user/last-updated zolo-cal/date-to-simple-string)
                                MESSAGES-START-TIME)]
    (->> user
         :user/user-identities
         (mapcat #(get-messages-for-user-identity % last-updated-string)))))

(defn get-contact-feed-for-contact [provider user-access-token contact-uid last-updated-string]
  (try
    (social/fetch-feed provider user-access-token contact-uid last-updated-string)
    (catch Exception e
      (print-vals "Error occurred processing contact-uid:" contact-uid (.getMessage e))
      nil)))

(defn get-contact-feeds-for-user-identity [ui contacts-by-provider last-updated-string]
  (let [{provider :identity/provider
         access-token :identity/auth-token} ui]
    (->> contacts-by-provider
         provider
         (map :social/provider-uid)
         (pmap #(get-contact-feed-for-contact provider access-token % last-updated-string))
         (apply concat))))

(defn get-contact-feeds-for-user [user]
  (let [contacts-by-provider (contact/provider-info-by-provider user)
        last-updated-string (or (-> user :user/last-updated zolo-cal/date-to-simple-string)
                                FEEDS-START-TIME)]
    (->> user
         :user/user-identities
         (mapcat #(get-contact-feeds-for-user-identity % contacts-by-provider last-updated-string)))))

(defn get-all-user-messages [user]
  (concat (get-messages-for-user user)
          (get-contact-feeds-for-user user)))

(defn update-messages [user]
  (->> user
       get-all-user-messages
       (refreshed-messages user)
       (assoc user :user/messages)
       demonic/insert))