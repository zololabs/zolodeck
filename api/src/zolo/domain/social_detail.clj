(ns zolo.domain.social-detail
  (:use zolodeck.utils.debug)
  (:require [zolodeck.utils.maps :as zolo-maps]
             [zolo.utils.domain :as domain]))

(def GIGYA-USER-IDENTITY-KEYS
  {:age :social/age
   :country :social/country
   :gender :social/gender
   :lastName :social/last-name
   :state :social/state
   :photoURL :social/photo-url
   :birthDay :social/birth-day
   :thumbnailURL :social/thumbnail-url
   :firstName :social/first-name
   :city :social/city
   :birthMonth :social/birth-month
   :nickname :social/nickname 
   :birthYear :social/birth-year
   :email :social/email 
   :profileURL :social/profile-url
   :providerUID :social/provider-uid
   :zip :social/zip
})

(defn gigya-user-identity->social-detail [gigya-user-identity]
  (-> gigya-user-identity
      (zolo-maps/update-all-map-keys GIGYA-USER-IDENTITY-KEYS)
      ;;TODO Need to set these enum values
      (dissoc :social/gender :social/provider)
      domain/force-schema-types))

(defn gigya-user-identities->social-details [gigya-user-identities]
  (map gigya-user-identity->social-detail gigya-user-identities))

;;TODO (Need to get first from social detail which has populated)
(defn first-name [social-details]
  (:social/first-name (first social-details)))

;;TODO (Need to get last from social detail which has populated)
(defn last-name [social-details]
  (:social/last-name (first social-details)))
