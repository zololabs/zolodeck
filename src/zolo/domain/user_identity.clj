(ns zolo.domain.user-identity
  (:use zolo.utils.debug)
  (:require [zolo.demonic.core :as demonic]
            [zolo.demonic.helper :as dh]))

;;TODO tests for this whole namespace

(defn find-by-provider-uid [u provider-uid]
  (->> u
       :user/user-identities
       (filter #(= provider-uid (:identity/provider-uid %)))
       first))

(defn- is-provider? [provider ui]
  (= provider (:identity/provider ui)))

(defn provider-uid [provider uis]
  (-> (filter #(is-provider? provider %) uis)
      first
      :identity/provider-uid))

(defn user-identity-info [ui]
  [(:identity/provider ui) (:identity/provider-uid ui)])

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
    (->> (demonic/run-query '[:find ?tx :in $ ?g
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

(defn- find-messages [ui]
  (->> (demonic/run-query '[:find ?m :in $ ?ui
                            :where
                            [?m :message/user-identity ?g]]
                          (:db/id ui))
       (map first)))

(defn messages [ui]
  (->> (find-messages ui)
       (map load-entity)))

(defn message-entitites [ui]
  (->> (find-messages ui)
       (map dh/load-from-db)))