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

(defn message-identifier [m]
  [(:message/provider m) (:message/message-id m)])

(defn refreshed-messages [user fresh-messages]
  (utils-domain/update-fresh-entities-with-db-id (:user/messages user)
                                                 fresh-messages
                                                 message-identifier
                                                 :message/guid))

(defn get-messages-for-user-identity [user-identity]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-messages provider access-token provider-uid)))

(defn update-contact-feed [user provider contact-provider-uid user-access-token]
  (social/fetch-feed provider user-access-token contact-provider-uid))

(defn get-messages-for-user [user]
  (->> user
       :user/user-identities
       (mapcat get-messages-for-user-identity)))

(defn get-contact-feed-for-contact [provider user-access-token contact-uid]
  (try
    (social/fetch-feed provider user-access-token contact-uid)
    (catch Exception e
      (print-vals "Error occurred processing contact-uid:" contact-uid (.getMessage e)))))

(defn get-contact-feeds-for-user-identity [ui contacts-by-provider]
  (let [{provider :identity/provider
         access-token :identity/auth-token} ui]
    (->> contacts-by-provider
         provider
         (map :social/provider-uid)
         (mapcat #(get-contact-feed-for-contact provider access-token %)))))

(defn get-contact-feeds-for-user [user]
  (let [contacts-by-provider (contact/provider-info-by-provider user)]
    (->> user
         :user/user-identities
         (mapcat #(get-contact-feeds-for-user-identity % contacts-by-provider)))))

(defn get-all-user-messages [user]
  (concat (get-messages-for-user user)
          (get-contact-feeds-for-user user)))

(defn update-messages [user]
  (->> user
       get-all-user-messages
       (refreshed-messages user)
       (assoc user :user/messages)
       demonic/insert))