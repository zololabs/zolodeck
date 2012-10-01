(ns zolo.social.facebook.users
  (:use zolodeck.utils.debug)
  (:require [zolo.social.core :as social]
            [zolo.utils.domain :as domain]
            [zolo.social.facebook.gateway :as gateway]            
            [zolodeck.utils.string :as string]))

(defn basic-info [extended-user-info]
  (domain/force-schema-types
   {:user/first-name (:first_name extended-user-info)
    :user/last-name (:last_name extended-user-info)
    :user/login-provider-uid (:uid extended-user-info)}))

(defn social-identity [provider access-token extended-user-info]
  (let [[month day year] (string/split "/" (:birthday_date extended-user-info))]
    (print-vals "bday:" [month day year])
    (domain/force-schema-types
     {:social/provider-uid (print-vals (:uid extended-user-info))
      :social/gender (social/gender-enum (:sex extended-user-info))
      :social/country (get-in extended-user-info [:current_location :country])
      :social/first-name (:first_name extended-user-info)
      :social/last-name (:last_name extended-user-info)
      :social/email (:email extended-user-info)
      :social/birth-day month
      :social/birth-month day
      :social/birth-year year
      :social/photo-url (:pic_big extended-user-info)
      :social/thumbnail-url (:pic_small extended-user-info)
      :social/profile-url (:profile_url extended-user-info)
      :social/provider (social/provider-enum provider)
      :social/auth-token access-token
      :social/state (get-in extended-user-info [:current_location :state])
      :social/city (get-in extended-user-info [:current_location :city])
      :social/zip (get-in extended-user-info [:current_location :zip])
      :social/nickname (:username extended-user-info)})))

(defn user-and-social-identity [access-token user-id]
  (let [extended-info (gateway/extended-user-info access-token user-id)
        basic-user (basic-info extended-info)
        identity (social-identity social/FACEBOOK access-token extended-info)]
    (assoc basic-user :user/social-identities [identity])))