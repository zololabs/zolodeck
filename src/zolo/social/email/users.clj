(ns zolo.social.email.users
  (:use zolo.utils.debug)
  (:require [zolo.social.email.gateway :as gateway]
            [zolo.utils.domain :as domain]))

(defn- to-user-identity [ui]
  (domain/force-schema-types
   {:identity/provider :provider/email
    :identity/provider-uid (:id ui)
    :identity/first-name (:first_name ui)
    :identity/last-name (:last_name ui)
    :identity/email (-> ui :email_addresses first)
    :identity/permissions-granted true}))

(defn user-identity [account-id]
  (-> account-id
      gateway/get-account
      to-user-identity))