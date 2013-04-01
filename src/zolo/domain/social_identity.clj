(ns zolo.domain.social-identity
  (:use zolo.utils.debug)
  (:require [zolo.utils.maps :as zolo-maps]
            [zolo.utils.domain :as domain]
            [zolodeck.demonic.core :as demonic]))

;;TODO test this name space

(defn social-identity-info [sd]
  [(:social/provider sd) (:social/provider-uid sd)])

(defn has-id? [si si-id]
  (= (social-identity-info si) si-id))

(defn social-identity [sis si-id]
  (first (filter #(has-id? % si-id) sis)))

;; (defn is-provider? [si provider]
;;   (= provider (:social/provider si)))

;; (defn is-fb? [si]
;;   (is-provider? si :provider/facebook))

;; (defn fb-social-identity [c]
;;   (->> c
;;        :contact/social-identities
;;        (filter is-fb?)
;;        first))

;; (defn fb-id [c]
;;   (-> c fb-social-identity :social/provider-uid))

;; (defn find-by-provider-and-provider-uid [provider provider-uid]
;;   ;;TODO Not using provider for now ... we need to start once we
;;   ;;figure how to store enum
;;   (when provider-uid
;;     (-> (demonic/run-query
;;          '[:find ?s :in $ ?provider-uid :where [?s :social/provider-uid ?provider-uid]] provider-uid)
;;         ffirst
;;         demonic/load-entity)))