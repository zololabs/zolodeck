(ns zolo.service.thread-service
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.thread :as t]
            [zolo.domain.message :as m]
            [zolo.store.user-store :as u-store]
            [zolo.store.message-store :as m-store]
            [zolo.domain.core :as d-core]
            [zolo.social.email.messages :as messages]))

(def REPLY-TO "reply_to")
(def FOLLOW-UP "follow_up")

;; Services
(defn find-threads [user-guid action]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (d-core/run-in-tz-offset (:user/login-tz u)
                             (condp = action
                               REPLY-TO (->> u t/find-reply-to-threads (domap #(t/distill u %)))
                               FOLLOW-UP (->> u t/find-follow-up-threads (domap #(t/distill u %)))
                               (throw (RuntimeException. (str "Unknown action type trying to find threads: " action)))))))

;;TODO Only will work for email
(defn load-thread-details [user-guid message-id]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (d-core/run-in-tz-offset (:user/login-tz u)
                             (if-let [m (m-store/find-by-id message-id)]
                               (if-let [account-id (-> m :message/user-identity :identity/auth-token)]
                                 (->> (messages/get-messages-for-thread account-id message-id)
                                      (t/messages->threads u)
                                      first
                                      (t/distill u)))))))

(defn update-thread-details [user-guid message-id done?]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (d-core/run-in-tz-offset (:user/login-tz u)
        (if-let [msg (m-store/find-by-id message-id)]
          (it-> msg
                (m/set-doneness it done?)
                (m-store/update-message it)
                (m/distill u it))))))