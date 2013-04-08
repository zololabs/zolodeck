(ns zolo.social.email.core
  (:use zolo.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.social.email.social-identities :as si]
            [zolo.social.email.messages :as messages]))

(defmethod social/fetch-social-identities :provider/email [provider access-token user-id date]
  (si/get-social-identities user-id))

(defmethod social/fetch-messages :provider/email [provider access-token user-id date]
  (messages/get-messages user-id))