(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        [slingshot.slingshot :only [throw+ try+]]
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        zolodeck.utils.debug)
  (:require [zolo.utils.domain :as domain]
            ;; [zolo.utils.gigya :as gigya-utils]
            ;; [zolo.gigya.core :as gigya]            
            [zolo.utils.readers :as readers]
            [zolo.domain.social-identity :as social-identity]
            [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [sandbar.auth :as sandbar]
            [clojure.set :as set]))

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
     (contact/update-contacts u)
     (print-vals "contacts done")
     ;;TODO Still looks like we are updating lot more messages than it
     ;;is present
     ;; (message/update-messages (reload u))
     ;; (print-vals "Messages done")
     ;; (update-scores (reload u))
     (reload u))
  ([]
     (fully-loaded-user (current-user))))


