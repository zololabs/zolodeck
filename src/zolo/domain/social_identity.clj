(ns zolo.domain.social-identity
  (:use zolo.utils.debug)
  (:require [zolo.utils.maps :as zolo-maps]
            [zolo.utils.domain :as domain]
            [zolo.domain.email-address :as addy]
            [zolo.demonic.core :as demonic]))

;;TODO test this name space

(defn social-identity-info [sd]
  [(:social/provider sd) (:social/provider-uid sd)])

(defn has-id? [si si-id]
  (= (social-identity-info si) si-id))

(defn social-identity [sis si-id]
  (first (filter #(has-id? % si-id) sis)))

(defn is-provider? [si provider]
  (= provider (:social/provider si)))

(defn is-fb? [si]
  (is-provider? si :provider/facebook))

(defn is-email? [si]
  (is-provider? si :provider/email))

(defn fb-social-identity [c]
  (->> c
       :contact/social-identities
       (filter is-fb?)
       first))

(defn fb-id [c]
  (-> c fb-social-identity :social/provider-uid))

(defn email-id [si]
  (if (is-email? si)
    (:social/provider-uid si)))

(defn mark-person-status [si]
  (if-not (is-email? si)
    (assoc si :social/not-a-person false)
    (assoc si :social/not-a-person (addy/matches-non-person-signal? (:social/provider-uid si)))))

(defn is-a-person [si]
  (not (:social/not-a-person si)))