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

(defn user-identity [access-token extended-user-info request-params]
  (let [[month day year] (string/split "/" (:birthday_date extended-user-info))]
    (domain/force-schema-types
     {:identity/provider-uid (:uid extended-user-info)
      :identity/gender (social/gender-enum (:sex extended-user-info))
      :identity/country (get-in extended-user-info [:current_location :country])
      :identity/first-name (:first_name extended-user-info)
      :identity/last-name (:last_name extended-user-info)
      :identity/email (:email extended-user-info)
      :identity/locale (:locale extended-user-info)
      :identity/birth-day day
      :identity/birth-month month
      :identity/birth-year year
      :identity/photo-url (:pic_big extended-user-info)
      :identity/thumbnail-url (:pic_small extended-user-info)
      :identity/profile-url (:profile_url extended-user-info)
      :identity/provider :provider/facebook
      :identity/auth-token access-token
      :identity/state (get-in extended-user-info [:current_location :state])
      :identity/city (get-in extended-user-info [:current_location :city])
      :identity/zip (get-in extended-user-info [:current_location :zip])
      :identity/nickname (:username extended-user-info)
      :identity/permissions-granted (:permissions_granted request-params)})))

(defn user-and-user-identity [access-token user-id request-params]
  (let [extended-info (gateway/extended-user-info access-token user-id)
        basic-user (basic-info extended-info)
        identity (user-identity access-token extended-info request-params)]
    (assoc basic-user :user/user-identities [identity])))