(ns zolo.social.facebook.contacts
  (:use zolodeck.utils.debug)
  (:require [zolodeck.utils.string :as string]
            [zolo.utils.domain :as domain]
            [zolo.social.core :as social]
            [zolo.utils.countries :as countries]
            [zolo.utils.logger :as logger]))

(defn contact-social-identity [friend]
  (let [[language country-code] (string/split "_" (:locale friend))
        [month day year] (string/split "/" (:birthday friend))]
    (domain/force-schema-types
     {:social/gender (social/gender-enum (:gender friend))
      :social/last-name (:last_name friend)
      :social/first-name (:first_name friend)
      :social/profile-url (:link friend)
      :social/country (countries/country-name-for country-code)
      :social/nickname (:username friend)
      :social/birth-day day
      :social/birth-month month
      :social/birth-year year
      :social/provider-uid (:id friend)
      :social/provider :provider/facebook
      :social/photo-url (get-in friend [:picture :data :url])
      })))

(defn contact-object [friend]
  {:contact/first-name (:first_name friend)
   :contact/last-name (:last_name friend)
   :contact/social-identities [(contact-social-identity friend)]})
