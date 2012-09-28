(ns zolo.social.facebook.core
  (:use zolodeck.utils.debug)
  (:require [zolo.utils.domain :as domain]
            [zolo.social.core :as social]
            [zolo.social.facebook.gateway :as gateway]))

(defn user-object [extended-user-info]
  (domain/force-schema-types
   {:user/first-name (:first_name extended-user-info)
    :user/last-name (:last_name extended-user-info)
    :user/login-provider-uid (:uid extended-user-info)}))

(defn split-birthdate [mmddyyyy]
  (->> (.split mmddyyyy "/")
       (into [])))

(defn social-identity [access-token extended-user-info]
  (let [[month day year] (split-birthdate (:birthday_date extended-user-info))]
    (domain/force-schema-types
     {:social/provider-uid (:uid extended-user-info)
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
      :social/provider (social/provider-enum social/FACEBOOK)
      :social/auth-token access-token
      :social/state (get-in extended-user-info [:current_location :state])
      :social/city (get-in extended-user-info [:current_location :city])
      :social/zip (get-in extended-user-info [:current_location :zip])
      :social/nickname (:username extended-user-info)})))

;; TODO add schema validation check for this API (facebook login)
(defmethod social/login-user social/FACEBOOK [request-params]
  (let [{access-token :accessToken user-id :userID signed-request :signedRequest} (get-in request-params [:providerLoginInfo :authResponse])
        info (gateway/extended-user-info access-token user-id)
        user-info (user-object info)
        identity (social-identity access-token info)]
    (print-vals "user-info:" user-info)
    (print-vals "social-info:" identity)
    (assoc user-info :user/social-identities [identity])))