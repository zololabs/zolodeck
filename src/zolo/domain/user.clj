(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.user-identity :as user-identity]
            [zolo.utils.maps :as zolo-maps]
            [zolo.utils.calendar :as zcal]
            [zolo.utils.logger :as logger]
            [zolo.setup.config :as conf]))

(def ^:dynamic *tz-offset-minutes* nil)

(defn- value-from-ui [u key]
  (-> u
      :user/user-identities
      first
      key))

;;Public
(defn first-name [u]
  (value-from-ui u :identity/first-name))

(defn last-name [u]
  (value-from-ui u :identity/last-name))

(defn provider-id [u provider]
  (condp  = provider 
    :provider/facebook (user-identity/fb-id u)
    (throw (RuntimeException. (str "Unknown provider specified: " provider)))))

(defn all-user-identities-info [u]
  (map user-identity/user-identity-info (:user/user-identities u)))

;;TODO test
(defn all-permissions-granted? [u]
  (every? #(:identity/permissions-granted %) (:user/user-identities u)))

(defn tz-offset-minutes []
  (or *tz-offset-minutes*
      (throw (RuntimeException. "User TZ is not set"))))

(defn client-date-time
  ([u]
     (client-date-time u (zcal/now-instant)))
  ([u t]
     (zcal/in-time-zone t (or (:user/login-tz u) 0))))

(defn needs-reminder-email? [{guid :user/guid emailing-start :user/emailing-start email-reminder-sent :user/email-reminder-sent}]
  (or (and (nil? email-reminder-sent)
           (nil? emailing-start))
      (and email-reminder-sent
           emailing-start
           (.before (zcal/to-inst (zcal/plus emailing-start (conf/email-frequency-minutes) :minutes)) (zcal/now-instant))
           (.before (zcal/to-inst (zcal/plus email-reminder-sent (conf/email-frequency-minutes) :minutes)) (zcal/now-instant)))))

(defn ui-from-guid [u ui-guid]
  (-> (filter #(= ui-guid (:identity/guid %)) (:user/user-identities u))
      first))

(defn update-with-extended-fb-auth-token [u token]
  (let [fb-ui (user-identity/fb-user-identity u)
        updated (merge fb-ui {:identity/auth-token token})]
    (if-not fb-ui u
            (zolo-maps/update-in-when u [:user/user-identities] user-identity/is-fb? updated))))

;;TODO Assumes Facebook .. Needs to be changed once we add another Login
(defn update-permissions-granted [u permissions-granted]
  (let [fb-ui (user-identity/fb-user-identity u)
        updated (merge fb-ui {:identity/permissions-granted permissions-granted})]
    (if-not fb-ui u
            (zolo-maps/update-in-when u [:user/user-identities] user-identity/is-fb? updated))))

(defn update-tz-offset [u tz-offset-in-mins]
  (merge u {:user/login-tz tz-offset-in-mins}))

(defn data-ready-in-remaining-seconds [u]
  (if (:user/data-ready-in u)
    (- (zcal/to-seconds (:user/data-ready-in u)) (zcal/to-seconds (zcal/now)))
    0))
