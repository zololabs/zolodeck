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
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.service.core :as service]
            [zolo.store.message-store :as m-store]
            [zolo.demonic.core :as demonic]
            [zolo.utils.calendar :as zcal]))

(defn- get-messages-for-user-identity [user-identity last-updated-seconds]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-messages provider access-token provider-uid last-updated-seconds)))

(defn- get-inbox-messages-for-user [u]
  (let [last-updated-seconds (zcal/to-seconds (message/get-last-message-date u))]
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

(def val-request
  {:provider [:required :string :empty-not-allowed]
   :text [:required :string :empty-not-allowed]
   :thread_id [:optional :string]
   :to [:required :collection :empty-not-allowed]
   :guid [:required :string :empty-not-allowed]
   })

(defn new-message [u params]
  (when u
    (let [{text :text thread-id :thread-id to-provider :provider to-provider-uids :to} params]
      (service/validate-request! params val-request)
      (let [provider (service/provider-string->provider-enum to-provider)
            from-uid (user/provider-id u provider)
            tmp-msg (message/create-temp-message from-uid to-provider-uids provider thread-id text)]
        (fb-chat/send-message u (first to-provider-uids) text)
        (m-store/append-temp-message u tmp-msg)
        tmp-msg))))