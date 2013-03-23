(ns zolo.store.user-store
  (:use zolodeck.utils.debug
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        [zolodeck.demonic.helper :only [load-from-db] :as demonic-helper]
        [zolodeck.demonic.loadable :only [entity->loadable] :as loadable])
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]))

(defn find-by-provider-and-provider-uid [provider provider-uid]
  (logger/debug (str "Finding user for provider : " provider " and provider-uid : " provider-uid))
  (when (and provider-uid (social/valid-provider? provider))
    (-> (demonic/run-query
         '[:find ?i :in $ ?provider ?provider-uid
           :where
           [?i :identity/provider-uid ?provider-uid]
           [?i :identity/provider ?provider]
           ] provider provider-uid)
        ffirst
        demonic-helper/load-from-db
        :user/_user-identities
        first
        loadable/entity->loadable)))

(defn find-by-guid [guid]
  (when guid
    (->> (if (string? guid) (java.util.UUID/fromString guid) guid)
         (demonic/run-query '[:find ?u :in $ ?guid :where [?u :user/guid ?guid]])
         ffirst
         demonic/load-entity)))

(defn save [new-values]
  (-> new-values
      demonic/insert-and-reload))