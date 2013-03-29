(ns zolo.domain.interaction
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.logger :as logger]
            [zolo.domain.message :as message]))

;;TODO Test this whole namespace

(defn within-interaction-time? [previous-msg next-msg]
  (let [gap-in-mins (zolo-cal/minutes-between (message/message-date previous-msg) (message/message-date next-msg))]
    (<= gap-in-mins 120)))

(defn- is-part-of? [interaction msg]
  (if (empty? interaction)
    true
    (within-interaction-time? (last interaction) msg)))

(defn- bucket-by-time [interactions msg]
  (if (is-part-of? (last interactions) msg)
    (conj-at-end (conj-at-end msg (last interactions)) (butlast interactions))
    (conj-at-end [msg] interactions)))

(defn messages->interactions [msgs]
  (reduce bucket-by-time [] msgs))

(defn interactions-by-contacts [imbc]
  (zolo-maps/transform-vals-with imbc (fn [c msgs] (messages->interactions msgs))))

;; (defn ibc [user]
;;   (-> user
;;       dom/inbox-messages-by-contacts
;;       interactions-by-contacts))