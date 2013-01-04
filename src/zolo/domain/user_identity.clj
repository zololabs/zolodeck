(ns zolo.domain.user-identity
  (:use zolodeck.utils.debug
        [zolodeck.demonic.core :only [run-query load-entity]]))

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

(defn fb-permissions-granted? [u]
  (-> u fb-user-identity :identity/permissions-granted))

(defn fb-permissions-time [u]
  (let [uig (-> u fb-user-identity :identity/guid)]
    (->> (run-query '[:find ?tx :in $ ?g
                      :where
                      [?u :identity/guid ?g]
                      [?u :identity/permissions-granted true ?tx]]
                    uig)
         ffirst
         load-entity
         :db/txInstant)))

(defn fb-email [u]
  (->> u
       fb-user-identity
       :identity/email))

