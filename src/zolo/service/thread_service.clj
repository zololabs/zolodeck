(ns zolo.service.thread-service
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.thread :as t]
            [zolo.store.user-store :as u-store]
            [zolo.social.email.gateway :as e-gateway]))

(def REPLY-TO "reply_to")
(def FOLLOW-UP "follow_up")

;; Services
(defn find-threads [user-guid action]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (condp = action
      REPLY-TO (->> u t/find-reply-to-threads (map #(t/distill u %)))
      FOLLOW-UP (->> u t/find-follow-up-threads (map #(t/distill u %)))
      (throw (RuntimeException. (str "Unknown action type trying to find threads: " action))))))

(defn load-gmail-thread-details [user-guid account-id thread-guid]
  (if-let [u (u-store/find-entity-by-guid user-guid)]
    (->> (e-gateway/get-gmail-thread account-id thread-guid)
         (t/messages->threads u)
         first
         t/distill)))