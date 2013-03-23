(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        [slingshot.slingshot :only [throw+ try+]]
        zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.utils.domain :as domain]
            [zolo.utils.readers :as readers]
            [zolo.domain.accessors :as dom]
            [zolo.domain.social-identity :as social-identity]
            [zolo.domain.user-identity :as user-identity]
            [zolo.domain.interaction :as interaction]
            [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.utils.maps :as zolo-maps]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [sandbar.auth :as sandbar]
            [clojure.set :as set]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.setup.config :as conf]))

(defn current-user []
  ;;(dissoc (sandbar/current-user) :username :roles)
  )

;; (defn count-users []
;;   (-> (demonic/run-query '[:find ?u :where [?u :user/guid]])
;;       count))

;; (defn creation-time [u]
;;   (->> (:user/guid u)
;;        (run-query '[:find ?tx :in $ ?g :where [?u :user/guid ?g ?tx]])
;;        ffirst
;;        load-entity
;;        :db/txInstant))

;; (defn find-all-users []
;;   (->> (demonic/run-query '[:find ?u :where [?u :user/guid]])
;;        (map first)
;;        (map demonic/load-entity)
;;        doall))

;; (defn- user-for-refresh [u]
;;   (-> (select-keys u [:user/guid :user/last-updated :user/refresh-started :user/fb-permissions-time])
;;       (assoc :user-temp/fb-permissions-time (user-identity/fb-permissions-time u))
;;       (assoc :user-temp/creation-time (creation-time u))))

;; ;; TODO use datalog to only find users with permissions granted
;; (defn find-all-users-for-refreshes []
;;   (->> (demonic/run-query '[:find ?u :where [?u :user/guid]])
;;        (map first)
;;        (map demonic-helper/load-from-db)
;;        ;(map #(select-keys % [:user/guid :user/last-updated :user/refresh-started :user/fb-permissions-time]))
;;        (map user-for-refresh)
;;        doall))

;;TODO Duplication find-by-guid
;; (defn find-by-guid [guid]
;;   (when guid
;;     (-> (demonic/run-query '[:find ?u :in $ ?guid :where [?u :user/guid ?guid]] guid)
;;         ffirst
;;         demonic/load-entity)))

;; (defn find-by-guid-string [guid-string]
;;   (when guid-string
;;     (find-by-guid (java.util.UUID/fromString guid-string))))

;; (defn find-by-login-provider-uid [login-provider-uid]
;;   (when login-provider-uid
;;     (-> (demonic/run-query '[:find ?u :in $ ?lpuid :where [?u :user/login-provider-uid ?lpuid]] login-provider-uid)
;;         ffirst
;;         demonic/load-entity)))



;; (defn provider-uid [user provider]
;;   (condp  = provider 
;;     :provider/facebook (user-identity/fb-id user)
;;     (throw+ {:type :bad-data :message (str "Unknown provider specified: " provider)})))

;; (defn reload [u]
;;   (find-by-guid (:user/guid u)))




;; ;; (defn update-creds [user creds]
;; ;;   (update-with-extended-fb-auth-token user (:access-token creds)))

;; ;; TODO move this into social core
;; ;; (defn extend-fb-token [u]
;; ;;   (update-with-extended-fb-auth-token u))

;; (defn new-suggestion-set [u set-name suggested-contacts]
;;   (-> u
;;       (assoc :user/suggestion-set-name set-name)
;;       (assoc :user/suggestion-set-contacts suggested-contacts)
;;       demonic/insert)
;;   suggested-contacts)

;; (defn suggestion-set [u suggestion-set-name]
;;   (if (= suggestion-set-name (:user/suggestion-set-name u))
;;     (:user/suggestion-set-contacts u)))

;; (defn update-scores [u]
;;   (let [ibc (-> u dom/inbox-messages-by-contacts interaction/interactions-by-contacts)]
;;     (doeach #(contact/update-score ibc %) (:user/contacts u))))

;; (defn stamp-updated-time [u]
;;   (-> u
;;       (assoc :user/last-updated (zolo-cal/now-instant))
;;       demonic/insert))

;; (defn stamp-refresh-start [u]
;;   (-> u
;;       (assoc :user/refresh-started (zolo-cal/now-instant))
;;       demonic/insert))

;; (defn refresh-user-data [u]
;;   (let [first-name (:user/first-name u)]
;;     (logger/trace first-name "RefreshUserData... starting now!")
;;     (stamp-refresh-start (reload u))
;;     (extend-fb-token (reload u))
;;     (contact/update-contacts (reload u))
;;     (logger/info first-name "Loaded contacts " (count (:user/contacts (reload u))))
;;     (message/update-inbox-messages (reload u))
;;     (logger/info first-name "Inbox messages done for " (count (:user/contacts (reload u))) " contacts")    
;;     (message/update-feed-messages-for-all-contacts (reload u))
;;     (logger/info first-name "Feed messages done for " (count (:user/contacts (reload u))) " contacts")
;;     (logger/info first-name "Refresh data done")
;;     nil))

;; (defn refresh-user-scores [u]
;;   (let [first-name (:user/first-name u)]
;;     (logger/trace first-name "Scoring " (count (:user/contacts (reload u))) " contacts")
;;     (update-scores (reload u))
;;     (logger/info first-name "scoring done")  
;;     (stamp-updated-time (reload u))
;;     (logger/info first-name "Refresh Score done")
;;     nil))

;; (defn been-processed? [u]
;;   (:user/last-updated u))

;; ;; CRUD Functions

;; (defn update [u new-values]
;;   (if-not u (throw (RuntimeException. "User should not be nil")))
;;   (-> u
;;       (merge new-values)
;;       demonic/insert))

(defn update-with-extended-fb-auth-token [u token]
  (let [fb-ui (user-identity/fb-user-identity u)
        updated (merge fb-ui {:identity/auth-token token})]
    (if-not fb-ui u
            (zolo-maps/update-in-when u [:user/user-identities] user-identity/is-fb? updated))))

(defn update-permissions-granted [u permissions-granted]
  (let [fb-ui (user-identity/fb-user-identity u)
        updated (merge fb-ui {:identity/permissions-granted permissions-granted})]
    (if-not fb-ui u
      (zolo-maps/update-in-when u [:user/user-identities] user-identity/is-fb? updated))))

(defn distill [u]
  (if u 
    {:guid (str (:user/guid u))
     :email (user-identity/fb-email u)}))
