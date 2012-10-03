(ns zolo.social.facebook.core
  (:use zolodeck.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.gateway :as gateway]
            [zolo.social.facebook.users :as users]            
            [zolo.social.facebook.contacts :as contacts]
            [zolo.social.facebook.messages :as messages]))

(defn login-creds [request-params]
  (let [creds (get-in request-params [:providerLoginInfo :authResponse])]
    (select-keys creds [:accessToken :userID])))

(defmethod social/provider-uid social/FACEBOOK [request-params cookies]
  (-> request-params
      login-creds
      :userID))

;; TODO add schema validation check for this API (facebook login)
(defmethod social/signup-user social/FACEBOOK [request-params cookies]
  (print-vals "FACEBOOK LOGIN params:" request-params)
  (print-vals "FACEBOOK LOGIN cookies:" cookies)
  (let [{access-token :accessToken user-id :userID} (login-creds request-params)]
    (users/user-and-social-identity access-token user-id)))

(defmethod social/fetch-contacts :provider/facebook [provider access-token user-id]
  (print-vals "FetchContacts:" provider)
  (let [friends (gateway/friends-list access-token user-id)]
    (doall (map contacts/contact-object friends))))

(defmethod social/fetch-messages :provider/facebook [provider access-token user-id]
  (print-vals "FetchMessages:" provider)
  (messages/fetch-inbox access-token))