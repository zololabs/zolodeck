(ns zolo.social.facebook.core
  (:use zolodeck.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.gateway :as gateway]
            [zolo.social.facebook.users :as users]            
            [zolo.social.facebook.contacts :as contacts]
            [zolo.social.facebook.messages :as messages]))

;; TODO add schema validation check for this API (facebook login)
(defmethod social/login-user social/FACEBOOK [request-params cookies]
  (print-vals "FACEBOOK LOGIN params:" request-params)
  (print-vals "FACEBOOK LOGIN cookies:" cookies)
  (let [login-creds (get-in request-params [:providerLoginInfo :authResponse])
        {access-token :accessToken user-id :userID} login-creds]
    (users/user-and-social-identity access-token user-id)))

(defmethod social/fetch-contacts :provider/facebook [provider access-token user-id]
  (print-vals "FetchContacts:" provider)
  (let [friends (gateway/friends-list access-token user-id)]
    (doall (map #(contacts/contact-object provider %) friends))))

(defmethod social/fetch-messages :provider/facebook [provider access-token user-id]
  (print-vals "FetchMessages:" provider)
  (messages/fetch-inbox access-token))