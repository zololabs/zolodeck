(ns zolo.service.distiller.contact
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.contact :as c]
            [zolo.domain.interaction :as interaction]
            [zolo.service.distiller.social-identity :as si-distiller]))

(defn distill-basic [contact]
  {:contact/first-name (c/first-name contact)
   :contact/last-name (c/last-name contact)
   :contact/guid (:contact/guid contact)
   :contact/muted (c/is-muted? contact)
   :contact/person (c/is-a-person? contact)
   :contact/picture-url (c/picture-url contact)
   :contact/social-identities (map si-distiller/distill (:contact/social-identities contact))})

(defn distill [contact ibc]
  (when contact
    (let [interactions (ibc contact)]
      (merge (distill-basic contact) {:contacted-today (c/is-contacted-today? contact ibc)
                                :contact/interaction-daily-counts (interaction/daily-counts interactions)}))))