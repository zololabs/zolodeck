(ns zolo.social.email.social-identities
  (:use zolo.utils.maps
        zolo.utils.clojure)
  (:require [zolo.social.email.gateway :as gateway]
            [zolo.utils.domain :as domain]))

(defn cio-contact->social-identity [c]
  (domain/force-schema-types
   {:social/nickname (:name c)
    :social/provider-uid (:email c)
    :social/provider :provider/email
    :social/photo-url (:thumbnail c)
    }))

(defn get-social-identities [cio-account-id date-in-seconds]
  (it-> cio-account-id
        (gateway/get-contacts it date-in-seconds)
        (domap cio-contact->social-identity it)))