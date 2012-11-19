(ns zolo.domain.user-identity
  (:use zolodeck.utils.debug))

(defn is-provider? [provider ui]
  (= provider (:identity/provider ui)))

(defn is-fb? [ui]
  (is-provider? :provider/facebook ui))

(defn fb-user-identity [u]
  (->> u
       :user/user-identities
       (filter is-fb?)
       first))

(defn fb-id [u]
  (-> u fb-user-identity :identity/provider-uid))

(defn fb-access-token [u]
  (-> u fb-user-identity :identity/auth-token))

