(ns zolo.service.distiller.contact
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.contact :as c]
            [zolo.domain.thread :as t]
            [zolo.domain.interaction :as interaction]
            [zolo.service.distiller.thread :as t-distiller]))

(defn- connection-history [u contact ibc]
  (->> (interaction/messages-from-ibc-for-contact contact ibc)
       t/messages->threads
       (domap #(t-distiller/distill u % "include_messages"))))

(defn distill [contact u ibc]
  (when (and u contact)
    (let [interactions (ibc contact)]
      (merge (t-distiller/distill-contact-basic contact)
             {:contacted-today (c/is-contacted-today? contact ibc)
              :contact/interaction-daily-counts (interaction/daily-counts interactions)
              :contact/history (connection-history u contact ibc)}))))