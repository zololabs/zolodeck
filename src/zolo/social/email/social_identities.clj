(ns zolo.social.email.social-identities
  (:use zolo.utils.maps
        zolo.utils.clojure
        zolo.utils.debug)
  (:require [zolo.social.email.gateway :as gateway]
            [zolo.utils.domain :as domain]
            [clojure.string :as string]))

(defn split-name [name email]
  (if name
    (let [sn (string/split name #" ")]
      {:first-name (first sn)
       :last-name (string/join " " (rest sn))})
    {:first-name email
     :last-name ""}))

(defn cio-contact->social-identity [c user-id]
  (let [c-name (split-name (:name c) (:email c))]
    (domain/force-schema-types
     {:social/nickname (:name c)
      :social/first-name (:first-name c-name)
      :social/last-name (:last-name c-name)
      :social/provider-uid (:email c)
      :social/ui-provider-uid user-id
      :social/email (:email c)
      :social/provider :provider/email
      :social/photo-url (:thumbnail c)
      :social/sent-count (:sent_count c)
      :social/received-count (:received_count c)})))

(defn get-social-identities [cio-account-id user-id date-in-seconds]
  (it-> cio-account-id
        (gateway/get-contacts it date-in-seconds)
        (domap #(cio-contact->social-identity % user-id) it)))