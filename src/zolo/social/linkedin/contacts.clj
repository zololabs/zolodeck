(ns zolo.social.linkedin.contacts
  (:use zolo.utils.debug)
  (:require [zolo.utils.domain :as domain]
            [zolo.utils.countries :as countries]))

(defn contact-social-identity [contact]
  (domain/force-schema-types
   {:social/provider-uid (:id contact)
    :social/profile-url (:publicProfileUrl contact)
    :social/country (countries/country-name-for (get-in contact [:location :country :code]))}))

(defn contact-object [contact]
  {:contact/first-name (:firstName contact)
   :contact/last-name (:lastName contact)
   :contact/social-identities [(contact-social-identity contact)]})