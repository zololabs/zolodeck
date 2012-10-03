(ns zolo.domain.social-identity
  (:use zolodeck.utils.debug)
  (:require [zolodeck.utils.maps :as zolo-maps]
            [zolo.utils.domain :as domain]
            [zolodeck.demonic.core :as demonic]))

;;TODO (Need to get first from social detail which has populated)
(defn first-name [social-identities]
  (:social/first-name (first social-identities)))

;;TODO (Need to get last from social detail which has populated)
(defn last-name [social-identities]
  (:social/last-name (first social-identities)))

(defn social-identity-info [sd]
  [(:social/provider sd) (:social/provider-uid sd)])

(defn find-by-provider-and-provider-uid [provider provider-uid]
  ;;TODO Not using provider for now ... we need to start once we
  ;;figure how to store enum
  (when provider-uid
    (-> (demonic/run-query '[:find ?s :in $ ?provider-uid :where [?s :social/provider-uid ?fb]] provider-uid)
        ffirst
        demonic/load-entity)))