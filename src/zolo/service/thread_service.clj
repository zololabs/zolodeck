(ns zolo.service.thread-service
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.thread :as t]))

;; Services
(defn find-threads [u action]
  (condp = action
    "reply_to" (t/find-reply-to-threads u)
    (throw (RuntimeException. (str "Unknown action type trying to find threads: " action)))))