(ns zolo.store.user-store
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        [zolodeck.demonic.helper :only [load-from-db] :as demonic-helper]
        [zolodeck.demonic.loadable :only [entity->loadable] :as loadable])
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.domain.user-identity :as user-identity]))

(defn creation-time [u]
  (->> (:user/guid u)
       (run-query '[:find ?tx :in $ ?g :where [?u :user/guid ?g ?tx]])
       ffirst
       load-entity
       :db/txInstant))

(defn- user-for-refresh [u]
  (-> (select-keys u [:user/guid :user/last-updated :user/refresh-started :user/fb-permissions-time])
      (assoc :user-temp/fb-permissions-time (user-identity/fb-permissions-time u))
      (assoc :user-temp/creation-time (creation-time u))))

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

;; TODO use datalog to only find users with permissions granted
;;TODO test
(defn find-all-users-for-refreshes []
  (->> (demonic/run-query '[:find ?u :where [?u :user/guid]])
       (map first)
       (map demonic-helper/load-from-db)
       ;(map #(select-keys % [:user/guid :user/last-updated :user/refresh-started :user/fb-permissions-time]))
       (map user-for-refresh)
       doall))

(defn stamp-updated-time [u]
  (-not-nil!-> u
               (assoc :user/last-updated (zolo-cal/now-instant))
               demonic/insert-and-reload))

(defn stamp-refresh-start [u]
  (-not-nil!-> u
               (assoc :user/refresh-started (zolo-cal/now-instant))
               demonic/insert-and-reload))

(defn reload [u]
  (-not-nil!-> (:user/guid u)
               find-by-guid))

(defn save [new-values]
  (-> new-values
      demonic/insert-and-reload))