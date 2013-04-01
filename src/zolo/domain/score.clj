(ns zolo.domain.score
  (:use zolo.utils.debug)
  (:require [zolo.domain.accessors :as dom]))

;; Factors

;; when was the interaction?
;; who started the interaction?
;; how many messages in the interaction?
;; how many words in the interaction?
;; ratio of sent to received messages
;; is it a group interaction? or 1-on-1?


;; (defmacro defscore [& body]
;;   ($age (cond
;;          (< $age 30) 30
;;          (< $age 90) 20
;;          :else 10)))

;; (defscore

;;   ($initiator (cond
;;                (= $initiator $user) 10
;;                :else 20))
;;   ($messages (cond
;;               (> $messages:count 10) 30
;;               (> $messages:count 5) 20
;;               :else 10))
;;   )

;;TODO test
(defn calculate [ibc c]
  (* 10 (count (ibc c))))


