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
            [zolo.utils.calendar :as zcal]
            [zolo.utils.domain :as d-utils]))

;;TODO Only will work for email
(defn load-thread-details [user-guid ui-guid message-id]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (let [ui (user/ui-from-guid u (to-uuid ui-guid))]
      (d-core/run-in-tz-offset (:user/login-tz u)
                               (if-let [m (m-store/find-by-ui-guid-and-id (:identity/guid ui) message-id)]
                                 (if-let [account-id (-> m :message/user-identity :identity/auth-token)]
                                   (let [existing-messages (m-store/find-messages-by-ui-guid-and-thread-id (:identity/guid ui) (:message/thread-id m))]
                                     (it-> (messages/get-messages-for-thread account-id message-id)
                                           (d-utils/update-fresh-entities-with-db-id existing-messages it :message/message-id :message/guid :message/done :message/follow-up-on)
                                           (map #(assoc % :message/user-identity (:message/user-identity m)) it)
                                           (t/messages->threads it)
                                           (first it)
                                           (t-distiller/distill u it "include_messages")))))))))

(defn update-thread-details [user-guid ui-guid message-id done? follow-up-on]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (let  [ui (user/ui-from-guid u (to-uuid ui-guid))]
      (d-core/run-in-tz-offset (:user/login-tz u)
                               (if-let [msg (m-store/find-by-ui-guid-and-id (:identity/guid ui) message-id)]
                                 (do (it-> msg
                                           (m/set-doneness it done?)
                                           (m/set-follow-up-on it (zcal/iso-string->inst follow-up-on))
                                           (m-store/update-message it))
                                     (->> (m-store/find-messages-by-ui-guid-and-thread-id (:identity/guid ui) (:message/thread-id msg))
                                          t/messages->threads
                                          first
                                          (t-distiller/distill u))))))))
