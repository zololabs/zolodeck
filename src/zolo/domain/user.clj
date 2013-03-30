(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.domain.user-identity :as user-identity]
            [zolodeck.utils.maps :as zolo-maps]
            [zolo.utils.logger :as logger]))

(defn current-user []
  ;;(dissoc (sandbar/current-user) :username :roles)
  )

;; (defn count-users []
;;   (-> (demonic/run-query '[:find ?u :where [?u :user/guid]])
;;       count))



;; (defn find-all-users []
;;   (->> (demonic/run-query '[:find ?u :where [?u :user/guid]])
;;        (map first)
;;        (map demonic/load-entity)
;;        doall))


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
    {:user/guid (str (:user/guid u))
     :user/email (user-identity/fb-email u)}))
