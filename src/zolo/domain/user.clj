(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        [slingshot.slingshot :only [throw+ try+]]
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        [zolodeck.demonic.helper :only [load-from-db] :as demonic-helper]
        [zolodeck.demonic.loadable :only [entity->loadable] :as loadable]
        zolodeck.utils.debug)
  (:require [zolo.utils.domain :as domain]
            [zolo.utils.readers :as readers]
            [zolo.domain.social-identity :as social-identity]
            [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [sandbar.auth :as sandbar]
            [clojure.set :as set]
            [zolo.utils.logger :as logger]))

(defn current-user []
  (dissoc (sandbar/current-user) :username :roles))

;;TODO Duplication find-by-guid
(defn find-by-guid [guid]
  (when guid
    (-> (demonic/run-query '[:find ?u :in $ ?guid :where [?u :user/guid ?guid]] guid)
        ffirst
        demonic/load-entity)))

(defn find-by-guid-string [guid]
  (find-by-guid (java.util.UUID/fromString guid)))

(defn find-by-login-provider-uid [login-provider-uid]
  (when login-provider-uid
    (-> (demonic/run-query '[:find ?u :in $ ?login-provider-uid :where [?u :user/login-provider-uid ?login-provider-uid]] login-provider-uid)
        ffirst
        demonic/load-entity)))

(defn find-by-provider-and-provider-uid [provider provider-uid]
  ;;TODO Not using provider for now ... we need to start once we
  ;;figure how to store enum
  (when provider-uid
    (-> (demonic/run-query
         '[:find ?s :in $ ?provider-uid :where [?s :social/provider-uid ?provider-uid]] provider-uid)
        ffirst
        demonic-helper/load-from-db
        :user/_social-identities
        first
        loadable/entity->loadable)))

(defn fb-social-identity [u]
  (->> u
       :user/social-identities
       (filter social-identity/is-fb?)
       first))

(defn fb-id [u]
  (-> u fb-social-identity :social/provider-uid))

(defn fb-access-token [u]
  (-> u fb-social-identity :social/auth-token))

(defn reload-using-login-provider-uid [u]
  (find-by-login-provider-uid (:user/login-provider-uid u)))

(defn signup-new-user [social-user]
  (-> social-user
      demonic/insert
      reload-using-login-provider-uid))

(defn reload [u]
  (find-by-guid (:user/guid u)))

(defn update-scores [u]
  (doall (map contact/update-score (:user/contacts u)))
  (reload u))

;;TODO Junk function. Need to design the app
(defn fully-loaded-user
  ([u]
     (if (empty? (:user/contacts u))
       (do
         (logger/debug "FullyLoadedUser... starting now!")
         (contact/update-contacts u)
         (logger/debug "Loaded contacts " (count (:user/contacts (reload u))))
         (message/update-messages (reload u))
         (logger/debug "Messages done")
         (update-scores (reload u))
         (reload u))
       (do
         (logger/debug "User if already fully loaded")
         u)))
  ([]
     (fully-loaded-user (current-user))))


