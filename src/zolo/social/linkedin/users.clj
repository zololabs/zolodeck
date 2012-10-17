(ns zolo.social.linkedin.users
  (:use zolodeck.utils.debug)
  (:require [zolo.utils.domain :as domain]
            [zolo.social.linkedin.gateway :as gateway]            
            [zolo.utils.countries :as countries]))

(defn basic-info [profile-info]
  (domain/force-schema-types
   {:user/first-name (:firstName profile-info)
    :user/last-name (:lastName profile-info)
    :user/login-provider-uid (:id profile-info)}))

(defn social-identity [auth-token profile-info]
  (let [{:keys [day month year]} (:dateOfBirth profile-info)]
    (domain/force-schema-types
     {:social/provider-uid (:id profile-info)
      :social/country (countries/country-name-for (get-in profile-info [:location :country :code]))
      :social/first-name (:firstName profile-info)
      :social/last-name (:lastName profile-info)
      :social/email (:emailAddress profile-info)
      :social/birth-day day
      :social/birth-month month
      :social/birth-year year
      :social/photo-url (:pictureUrl profile-info)
      :social/profile-url (:publicProfileUrl profile-info)
      :social/provider :provider/linkedin
      :social/auth-token auth-token})))

(defn user-and-social-identity [auth-token-map]
  (let [profile (gateway/profile-info (:oauth_token auth-token-map) (:oauth_token_secret auth-token-map))
        basic (basic-info profile)
        si (social-identity (prn-str auth-token-map) profile)]
    (assoc basic :user/social-identities [si])))