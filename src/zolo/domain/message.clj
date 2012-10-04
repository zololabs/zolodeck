(ns zolo.domain.message
  (:use zolodeck.utils.debug)
  (:require [clojure.set :as set]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolo.domain.contact :as contact]
            [zolo.domain.social-identity :as social-identity]
            [zolo.social.core :as social]            
            [zolodeck.demonic.schema :as schema]
            [zolodeck.demonic.core :as demonic]))

;;TODO Need to find a better place for this function
(defn user-provider-infos [user]
  (->> user
      :user/social-identities
      (map social-identity/social-identity-info)))

(defn dissoc-user-messages [user grouped-messages]
  (reduce (fn [msgs user-provider-info]
            (dissoc msgs user-provider-info))
          grouped-messages
          (user-provider-infos user)))

;;TODO Duplication
(defn message-from-provider-info [m]
  [(:message/provider m) (:message/from m)])

(defn message-to-provider-info [m]
  [(:message/provider m) (:message/to m)])

(defn group-by-provider-info [user messages]
  (let [grouped-by-from (group-by message-from-provider-info messages)
        grouped-by-to (group-by message-to-provider-info messages)
        grouped  (merge-with concat grouped-by-from grouped-by-to)]
    (dissoc-user-messages user grouped)))

(defn process-contact-messages [user provider-info fresh-messages]
  (let [contact (or (contact/find-contact-by-provider-info user provider-info)
                    (contact/create-contact user provider-info))]
    (assoc contact :contact/messages
           (utils-domain/update-fresh-entities-with-db-id (:contact/messages contact) fresh-messages :message/message-id :message/guid))))

(defn merge-messages [user fresh-messages]
  (let [grouped (group-by-provider-info user fresh-messages)]
    (print-vals "Grouped count:" (count grouped))
    (map (fn [[provider-info msgs]]
           (process-contact-messages user provider-info msgs)) grouped)))

(defn update-messages-for-social-identity [user social-identity]
  (let [{provider :social/provider
         access-token :social/auth-token
         provider-uid :social/provider-uid} social-identity]
    (print-vals "provider, at, uid:" provider access-token provider)
    (->> (social/fetch-messages provider access-token provider-uid)
         print-vals
         (merge-messages user)
         (map demonic/insert)
         doall)))

(defn update-messages [user]
  (doseq [si (:user/social-identities user)]
    (update-messages-for-social-identity user si)))

