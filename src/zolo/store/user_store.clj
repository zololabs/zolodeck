(ns zolo.store.user-store
  (:use zolodeck.utils.debug
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        [zolodeck.demonic.helper :only [load-from-db] :as demonic-helper]
        [zolodeck.demonic.loadable :only [entity->loadable] :as loadable])
  (:require [zolo.utils.logger :as logger]))

(defn find-by-provider-and-provider-uid [provider provider-uid]
  (logger/debug (str "Finding user for provider : " provider " and provider-uid : " provider-uid))
  (when provider-uid
    (-> (demonic/run-query
         '[:find ?i :in $ ?provider-uid :where [?i :identity/provider-uid ?provider-uid]] provider-uid)
        ffirst
        demonic-helper/load-from-db
        :user/_user-identities
        first
        loadable/entity->loadable)))

(defn save [new-values]
  (-> new-values
      demonic/insert-and-reload))