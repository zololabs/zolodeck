(ns zolo.store.user-identity-store
  (:use zolo.utils.debug
        zolo.utils.clojure
        [zolo.demonic.core :only [insert run-query load-entity] :as demonic]
        [zolo.demonic.helper :only [load-from-db] :as demonic-helper]
        [zolo.demonic.loadable :only [entity->loadable] :as loadable])
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [zolo.utils.calendar :as zolo-cal]))

;;TODO test
(defn find-by-provider-and-email [provider email]
  (logger/debug (str "Finding user for provider : " provider " and email : " email))
  (when (and email (social/valid-provider? provider))
    (-> (demonic/run-query
         '[:find ?i :in $ ?provider ?email
           :where
           [?i :identity/email ?email]
           [?i :identity/provider ?provider]
           ] provider email)
        ffirst
        demonic-helper/load-from-db
        loadable/entity->loadable)))

