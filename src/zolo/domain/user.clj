(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        [slingshot.slingshot :only [throw+ try+]]
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        [zolodeck.demonic.helper :only [load-from-db] :as demonic-helper]
        [zolodeck.demonic.loadable :only [entity->loadable] :as loadable]
        zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.utils.domain :as domain]
            [zolo.utils.readers :as readers]
            [zolo.domain.social-identity :as social-identity]
            [zolo.domain.user-identity :as user-identity]
            [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.utils.maps :as zolo-maps]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [sandbar.auth :as sandbar]
            [clojure.set :as set]
            [zolo.utils.logger :as logger]))

(defn current-user []
  (dissoc (sandbar/current-user) :username :roles))

(defn find-all-users []
  (->> (demonic/run-query '[:find ?u :where [?u :user/guid]])
       (map first)
       (map demonic/load-entity)
       doall))

(defn find-all-users-for-refreshes []
  (->> (demonic/run-query '[:find ?u :where [?u :user/guid]])
       (map first)
       (map demonic-helper/load-from-db)
       (map #(select-keys % [:user/guid :user/last-updated :user/refresh-started]))
       doall))

;;TODO Duplication find-by-guid
(defn find-by-guid [guid]
  (when guid
    (-> (demonic/run-query '[:find ?u :in $ ?guid :where [?u :user/guid ?guid]] guid)
        ffirst
        demonic/load-entity)))

(defn find-by-guid-string [guid-string]
  (when guid-string
    (find-by-guid (java.util.UUID/fromString guid-string))))

(defn find-by-login-provider-uid [login-provider-uid]
  (when login-provider-uid
    (-> (demonic/run-query '[:find ?u :in $ ?lpuid :where [?u :user/login-provider-uid ?lpuid]] login-provider-uid)
        ffirst
        demonic/load-entity)))

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

(defn fb-user-identity [u]
  (->> u
       :user/user-identities
       (filter user-identity/is-fb?)
       first))

(defn fb-id [u]
  (-> u fb-user-identity :identity/provider-uid))

(defn fb-access-token [u]
  (-> u fb-user-identity :identity/auth-token))

(defn reload [u]
  (find-by-guid (:user/guid u)))

(defn reload-by-login-provider-uid [u]
  (-> u
      :user/login-provider-uid
      find-by-login-provider-uid))

;; TODO - reload-by-login-provider-uid assumes unique lpuid across all networks
(defn signup-new-user [social-user]
  (-> social-user
      demonic/insert
      reload-by-login-provider-uid))

(defn update-scores [u]
  (doeach #(contact/update-score u %) (:user/contacts u))
  (reload u))

(defn stamp-updated-time [u]
  (-> u
      (assoc :user/last-updated (zolo-cal/now-instant))
      demonic/insert))

(defn stamp-refresh-start [u]
  (-> u
      (assoc :user/refresh-started (zolo-cal/now-instant))
      demonic/insert))

(defn refresh-user-data [u]
  (logger/trace "RefreshUserData... starting now!")
  (stamp-refresh-start (reload u))
  (contact/update-contacts u)
  (logger/debug "Loaded contacts " (count (:user/contacts (reload u))))
  (message/update-messages (reload u))
  (logger/debug "Messages done")
  (update-scores (reload u))
  (stamp-updated-time (reload u))
  (reload u))

;;TODO Junk function. Need to design the app
(defn fully-loaded-user
  ([u]
     (if (empty? (:user/contacts u))
       (refresh-user-data u)
       (do
         (logger/debug "User is already fully loaded")
         u)))
  ([]
     (fully-loaded-user (current-user))))
