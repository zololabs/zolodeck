(ns zolo.service.thread-service
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.thread :as t]
            [zolo.store.user-store :as u-store]))

;; Services
(defn find-threads [user-guid action]
  (if-let [u (u-store/find-by-guid user-guid)]
    (condp = action
      "reply_to" (t/find-reply-to-threads u)
      "follow_up" (t/find-follow-up-threads u)
      (throw (RuntimeException. (str "Unknown action type trying to find threads: " action))))))