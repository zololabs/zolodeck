(ns zolo.service.stats-service
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.service.core :as service]
            [zolo.domain.contact :as contact]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.core :as d-core]))


(defn contact-stats [u]
  (when u
    (d-core/run-in-tz-offset (:user/login-tz u)
                             (let [ibc (interaction/ibc u)
                                   contacts (sort-by contact/contact-score (:user/contacts u))]
                               {:total (count contacts)
                                :strong (count (contact/strong-contacts u))
                                :medium (count (contact/medium-contacts u))
                                :weak   (count (contact/weak-contacts u))
                                :quartered (count (contact/contacts-not-contacted-for-days ibc 90))
                                :strongest-contact (contact/distill (last contacts) ibc)
                                :weakest-contact (contact/distill (first contacts) ibc)}))))
