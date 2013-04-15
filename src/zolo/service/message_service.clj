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

(def val-request
  {:provider [:required :string :empty-not-allowed]
   :text [:required :string :empty-not-allowed]
   :thread_id [:optional :string]})

 ;; Services
(defn update-inbox-messages [u]
  (when u
    (->> u
         m-store/delete-temp-messages
         get-inbox-messages-for-user
         (m-store/append-messages u))))

(defn new-message [u c params]
  (when (and u c)
    (service/validate-request! params val-request)
    (let [provider (service/provider-string->provider-enum (:provider params))
          from-uid (user/provider-id u provider)
          to-uid (contact/provider-id c provider)]
;;      (message/create-temp-message from-uid to-uid provider (:thread_id params) (:text params))
      (fb-chat/send-message u to-uid (:text params))
      (->> (message/create-temp-message from-uid to-uid provider (:thread_id params) (:text params))
           (m-store/append-temp-message u))
      )))