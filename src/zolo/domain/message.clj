(ns zolo.domain.message
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [clojure.set :as set]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolo.domain.accessors :as dom]
            [zolo.domain.contact :as contact]            
            [zolo.domain.social-identity :as social-identity]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.core :as social]            
            [zolodeck.demonic.schema :as schema]
            [zolodeck.demonic.core :as demonic]
            [zolo.utils.logger :as logger]))

(def MESSAGES-START-TIME-SECONDS (-> #inst "2000-10-22" .getTime zolo-cal/to-seconds))

(defn feeds-start-time-seconds []
  (-> (zolo-cal/now-joda)
      (zolo-cal/minus 1 :week)
      (zolo-cal/to-seconds)))

(defn message-identifier [m]
  [(dom/message-provider m) (dom/message-id m)])

(defn refreshed-messages [user fresh-messages]
  (utils-domain/update-fresh-entities-with-db-id (:user/messages user)
                                                 fresh-messages
                                                 message-identifier
                                                 dom/message-guid))

(defn get-messages-for-user-identity [user-identity last-updated-string]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-messages provider access-token provider-uid last-updated-string)))

(defn get-messages-for-user [user]
  (let [date (->> user
                  dom/inbox-messages-for-user
                  (remove dom/is-temp-message?)
                  (sort-by :message/date)
                  last
                  :message/date)
        seconds (if date (-> date .getTime zolo-cal/to-seconds))]
    (->> user
         :user/user-identities
         (mapcat #(get-messages-for-user-identity % (or seconds MESSAGES-START-TIME-SECONDS))))))

(defn delete-temp-messages [user]
  (->> user
       :user/temp-messages
       (doeach demonic/delete)))

(defn update-inbox-messages [user]
  (->> user
       get-messages-for-user
       (demonic/append-multiple user :user/messages))
  (delete-temp-messages user))

(defn- update-messages-for-contact-and-provider [user feed-messages si]
  (try-catch
   (let [{contact-uid :social/provider-uid provider :social/provider} si
         auth-token (-> user (dom/user-identity-for-provider provider) :identity/auth-token)
         fmg (group-by dom/message-provider feed-messages)
         date (->> provider fmg (sort-by dom/message-date) last dom/message-date)
         seconds (if date (-> date .getTime zolo-cal/to-seconds))
         feed-messages (social/fetch-feed provider auth-token contact-uid (or seconds (feeds-start-time-seconds)))]
     (demonic/append-multiple user :user/messages feed-messages))))

(defn update-messages-for-contact [user contact]
  (let [fmbc (-> user dom/feed-messages-by-contacts)
        feed-messages (fmbc contact)
        identities (:contact/social-identities contact)]
    (doeach #(update-messages-for-contact-and-provider user feed-messages %) identities)))

(defn update-feed-messages-for-all-contacts [user]
  (->> user
       :user/contacts
       (pdoeach #(update-messages-for-contact user %) 20 true)))

(defn create-new [from-user provider-string to-uid text thread-id]
  (let [m {:temp-message/provider (social/provider-enum (.toUpperCase provider-string))
           :temp-message/from (user-identity/fb-id from-user)
           :temp-message/to to-uid
           :temp-message/text text
;           :temp-message/thread-id thread-id
           :temp-message/mode "INBOX"
           :temp-message/date (zolo-cal/now-instant)}]
    (demonic/append-single from-user :user/temp-messages m)
    m))