(ns zolo.service.thread-service
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.thread :as t]
            [zolo.domain.message :as m]
            [zolo.domain.user :as user]
            [zolo.store.user-store :as u-store]
            [zolo.store.message-store :as m-store]
            [zolo.domain.core :as d-core]
            [zolo.social.email.messages :as messages]
            [zolo.service.distiller.message :as m-distiller]
            [zolo.service.distiller.thread :as t-distiller]
            [zolo.utils.calendar :as zcal]))

;;TODO Only will work for email
(defn load-thread-details [user-guid ui-guid message-id]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (let [ui (user/ui-from-guid u (to-uuid ui-guid))]
      (d-core/run-in-tz-offset (:user/login-tz u)
                               (if-let [m (m-store/find-by-ui-guid-and-id (:identity/guid ui) message-id)]
                                 (if-let [account-id (-> m :message/user-identity :identity/auth-token)]
                                   (->> (messages/get-messages-for-thread account-id message-id)
                                        t/messages->threads
                                        first
                                        (t-distiller/distill u))))))))

(defn update-thread-details [user-guid ui-guid message-id done? follow-up-on]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (let  [ui (user/ui-from-guid u (to-uuid ui-guid))]
      (d-core/run-in-tz-offset (:user/login-tz u)
                               (if-let [msg (m-store/find-by-ui-guid-and-id (:identity/guid ui) message-id)]
                                 (it-> msg
                                       (m/set-doneness it done?)
                                       (m/set-follow-up-on it (zcal/iso-string->inst follow-up-on))
                                       (m-store/update-message it)
                                       (m-distiller/distill u it)))))))
