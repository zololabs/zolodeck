(ns zolo.social.email.social-identities
  (:use zolo.utils.maps
        zolo.utils.clojure)
  (:require [zolo.social.email.gateway :as gateway]
            [zolo.utils.domain :as domain]
            [clojure.string :as string]))

(defn split-name [name]
  (let [sn (string/split name #" ")]
    {:first-name (first sn)
     :last-name (string/join " " (rest sn))}))

(defn cio-contact->social-identity [c]
  (let [c-name (split-name (:name c))]
    (domain/force-schema-types
     {:social/nickname (:name c)
      :social/first-name (:first-name c-name)
      :social/last-name (:last-name c-name)
      :social/provider-uid (:email c)
      :social/provider :provider/email
      :social/photo-url (:thumbnail c)})))

(defn get-social-identities [cio-account-id date-in-seconds]
  (it-> cio-account-id
        (gateway/get-contacts it date-in-seconds)
        (domap cio-contact->social-identity it)))