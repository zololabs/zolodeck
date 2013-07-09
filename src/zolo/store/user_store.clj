(ns zolo.store.user-store
  (:use zolo.utils.debug
        zolo.utils.clojure
        [zolo.demonic.core :only [insert run-query load-entity] :as demonic]
        [zolo.demonic.helper :only [load-from-db] :as demonic-helper]
        [zolo.demonic.loadable :only [entity->loadable] :as loadable])
  (:require [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.domain.user-identity :as user-identity]))

(defn creation-time [u]
  (->> (:user/guid u)
       (run-query '[:find ?tx :in $ ?g :where [?u :user/guid ?g ?tx]])
       ffirst
       load-entity
       :db/txInstant))

;;TODO test
(defn count-users []
  (-> (demonic/run-query '[:find ?u :where [?u :user/guid]])
      count))

;; TODO get rid of -temp nomenclature for these computed fields 
(defn- user-for-refresh [u]
  (-> (select-keys u [:user/guid :user/last-updated :user/refresh-started :user/fb-permissions-time])
      (assoc :user-temp/fb-permissions-time (user-identity/fb-permissions-time u))
      (assoc :user-temp/creation-time (creation-time u))))

(defn find-by-provider-and-property [provider p-name p-value]
  (logger/debug (str "Finding user for provider : " provider " and property : " p-name "," p-value))
  (when (and p-name p-value (social/valid-provider? provider))
    (-> (demonic/run-query
         '[:find ?i :in $ ?provider ?p-name ?p-value
           :where
           [?i ?p-name ?p-value]
           [?i :identity/provider ?provider]
           ] provider p-name p-value)
        ffirst
        demonic-helper/load-from-db
        :user/_user-identities)))

(defn find-by-provider-and-provider-uid [provider provider-uid]
  (find-by-provider-and-property provider :identity/provider-uid provider-uid))

(defn find-by-provider-and-auth-token [provider auth-token]
  (find-by-provider-and-property provider :identity/auth-token auth-token))

(defn find-entity-id-by-guid [guid]
  (when guid
    (->> (if (string? guid) (java.util.UUID/fromString guid) guid)
         (demonic/run-query '[:find ?u :in $ ?guid :where [?u :user/guid ?guid]])
         ffirst)))

(defn find-entity-by-guid [guid]
  (-> guid
      find-entity-id-by-guid
      load-from-db))

(defn find-by-guid [guid]
  (-> guid
      find-entity-id-by-guid
      demonic/load-entity))

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