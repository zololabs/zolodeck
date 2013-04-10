(ns zolo.social.facebook.core
  (:use zolo.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.gateway :as gateway]
            [zolo.social.facebook.users :as users]            
            [zolo.social.facebook.social-identities :as sis]
            [zolo.social.facebook.messages :as messages]
            [zolo.utils.logger :as logger]
            [zolo.utils.maps :as zolo-maps]))

(defn login-creds [request-params]
  {:access-token (:access_token request-params)
   :user-id (:login_provider_uid request-params)})

(defmethod social/provider-uid social/FACEBOOK [request-params]
  (-> request-params
      :login_provider_uid))

(defmethod social/fetch-creds social/FACEBOOK [request-params]
  (login-creds request-params))

; TODO add schema validation check for this API (facebook login)
;;TODO Rename this to Create
(defmethod social/fetch-user-identity social/FACEBOOK [request-params]
  (logger/trace "FACEBOOK LOGIN params:" request-params)
  (let [{:keys [access-token user-id]} (login-creds request-params)]
    (users/user-and-user-identity access-token user-id request-params)))

(defmethod social/fetch-social-identities :provider/facebook [provider access-token user-id date]
  ;(logger/trace "FetchContacts:" provider)
  (let [friends (gateway/friends-list access-token user-id)]
    (doall (map sis/social-identity-object friends))))

(defmethod social/fetch-messages :provider/facebook [provider access-token user-id date]
  ;(logger/trace "FetchMessages:" provider)
  (messages/fetch-messages access-token user-id date))

(defmethod social/fetch-feed :provider/facebook [provider access-token user-id date]
  ;(logger/trace "FetchFeed:" provider user-id)
  (messages/fetch-feed access-token user-id date))

(defmethod social/fetch-contact-feeds :provider/facebook [provider access-token last-updated-string provider-uids]
  ;(logger/trace "FetchContactFeeds:" provider-uids)
  (messages/fetch-all-contact-feeds access-token last-updated-string provider-uids))