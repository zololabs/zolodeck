(ns zolo.social.facebook.core
  (:use zolodeck.utils.debug)
  (:require [zolo.utils.domain :as domain]
            [zolo.utils.countries :as countries]
            [zolo.social.core :as social]
            [zolo.social.facebook.gateway :as gateway]))

(defn user-object [extended-user-info]
  (domain/force-schema-types
   {:user/first-name (:first_name extended-user-info)
    :user/last-name (:last_name extended-user-info)
    :user/login-provider-uid (:uid extended-user-info)}))


;; TODO create and move a 'split-at' function into utils.clojure
(defn split-birthdate [mmddyyyy]
  (if mmddyyyy
    (->> (.split mmddyyyy "/")
         (into []))))

(defn split-locale [locale]
  (if locale
    (->> (.split locale "_")
         (into []))))

(defn user-social-identity [provider access-token extended-user-info]
  (let [[month day year] (split-birthdate (:birthday_date extended-user-info))]
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

(defn contact-social-identity [provider-enum friend]
  (let [[language country-code] (split-locale (:locale friend))
        [month day year] (split-birthdate (:birthday friend))]
    (domain/force-schema-types
     {:social/gender (social/gender-enum (:gender friend))
      :social/last-name (:last_name friend)
      :social/first-name (:first_name friend)
      :social/profile-url (:link friend)
      :social/country (countries/country-name-for country-code)
      :social/nickname (:username friend)
      :social/birth-day month
      :social/birth-month day
      :social/birth-year year
      :social/provider-uid (:id friend)
      :social/provider provider-enum
      :social/photo-url (:picture friend)
      })))

(defn contact-object [provider friend]
  {:contact/first-name (:first_name friend)
   :contact/last-name (:last_name friend)
   :contact/social-identities [(contact-social-identity provider friend)]})

;; TODO add schema validation check for this API (facebook login)
(defmethod social/login-user social/FACEBOOK [request-params]
  (let [{access-token :accessToken user-id :userID signed-request :signedRequest} (get-in request-params [:providerLoginInfo :authResponse])
        info (gateway/extended-user-info access-token user-id)
        user-info (user-object info)
        identity (user-social-identity social/FACEBOOK access-token info)]
    (print-vals "user-info:" user-info)
    (print-vals "social-info:" identity)
    (assoc user-info :user/social-identities [identity])))

(defmethod social/fetch-contacts :provider/facebook [provider access-token user-id]
  (print-vals "UpdateContacts: FACEBOOK" )
  (let [friends (print-vals "Friends:" (gateway/friends-list access-token user-id))]
    (doall (map #(contact-object provider %) friends))))