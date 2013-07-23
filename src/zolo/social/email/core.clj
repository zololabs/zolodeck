(ns zolo.social.email.core
  (:use zolo.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.social.email.gateway :as gateway]
            [zolo.social.email.users :as users]
            [zolo.social.email.social-identities :as si]
            [zolo.social.email.messages :as messages]
            [zolo.utils.logger :as logger]))

(defn login-creds [request-params]
  {:access-token (:access_token request-params)
   :user-id (:login_provider_uid request-params)})

;;TODO No need for this to be multimethod
(defmethod social/provider-uid social/EMAIL [request-params]
  (-> request-params
      :login_provider_uid))

;;TODO No need for this to be multimethod
(defmethod social/fetch-creds social/EMAIL [request-params]
  (login-creds request-params))

(defmethod social/fetch-user-identity social/EMAIL [params]
  (logger/trace "EMAIL fetch-user-identity params:" params)
  (let [{cio-account-id :access_token} params]
    (users/user-identity cio-account-id)))

(defmethod social/fetch-social-identities :provider/email [provider access-token user-id date-in-seconds]
  (si/get-social-identities access-token user-id date-in-seconds))

(defmethod social/fetch-messages :provider/email [provider access-token user-id date-in-seconds]
  (gateway/reset-account-status access-token)
  (messages/get-messages access-token date-in-seconds))

(defmethod social/fetch-deleted-messages :provider/email [provider access-token user-id date-in-seconds]
  (messages/get-deleted-messages access-token date-in-seconds))

(defmethod social/send-message :provider/email [provider access-token from-email to-emails thead-id reply-to-message-id subject message]
  (gateway/send-email access-token from-email to-emails reply-to-message-id subject message))