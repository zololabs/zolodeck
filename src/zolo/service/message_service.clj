(ns zolo.service.message-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolo.store.user-store :as u-store]
            [zolo.utils.logger :as logger]
            [zolo.service.core :as service]
            [zolo.store.message-store :as m-store]
            [zolodeck.demonic.core :as demonic]))

(defn- get-messages-for-user-identity [user-identity last-updated-seconds]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-messages provider access-token provider-uid last-updated-seconds)))

(defn- get-inbox-messages-for-user [u]
  (let [last-updated-seconds (message/get-last-message-date u)]
    (->> u
         :user/user-identities
         (mapcat #(get-messages-for-user-identity % last-updated-seconds)))))

 ;; Services
(defn update-inbox-messages [u]
  (when u
    (->> u
         m-store/delete-temp-messages
         get-inbox-messages-for-user
         (m-store/append-messages u))))