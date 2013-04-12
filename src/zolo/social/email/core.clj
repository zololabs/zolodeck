(ns zolo.social.email.core
  (:use zolo.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.social.email.users :as users]
            [zolo.social.email.social-identities :as si]
            [zolo.social.email.messages :as messages]))

(defmethod social/fetch-user-identity social/EMAIL [params]
  (logger/trace "EMAIL fetch-user-identity params:" params)
  (let [{:keys [account-id]} (:account-id params)]
    (users/user-identity account-id)))

(defmethod social/fetch-social-identities :provider/email [provider access-token user-id date-in-seconds]
  (si/get-social-identities user-id date-in-seconds))

(defmethod social/fetch-messages :provider/email [provider access-token user-id date-in-seconds]
  (messages/get-messages user-id date-in-seconds))