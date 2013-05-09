(ns zolo.social.email.users
  (:use zolo.utils.debug)
  (:require [zolo.social.email.gateway :as gateway]
            [zolo.utils.domain :as domain]))

(defn- to-user-identity [cio-account]
  (domain/force-schema-types
   {:identity/provider :provider/email
    :identity/provider-uid (:id cio-account)
    :identity/first-name (:first_name cio-account)
    :identity/last-name (:last_name cio-account)
    :identity/email (-> cio-account :email_addresses first)
    :identity/permissions-granted true}))

(defn user-identity [cio-account-id]
  (-> cio-account-id
      gateway/get-account
      to-user-identity))