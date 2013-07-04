(ns zolo.service.stats-service
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.service.core :as service]
            [zolo.domain.contact :as contact]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.stats.interaction-distribution :as s-int-dist]
            [zolo.domain.core :as d-core]
            [zolo.utils.math :as zmath]
            [zolo.service.distiller.contact :as c-distiller]))


(defn contact-stats [u]
  (when u
    (d-core/run-in-tz-offset (:user/login-tz u)
                             (let [contacts (sort-by contact/contact-score (contact/person-contacts u))
                                   ibc (interaction/ibc u contacts)]
                               {:total (count contacts)
                                :strong (count (contact/strong-contacts u))
                                :medium (count (contact/medium-contacts u))
                                :weak   (count (contact/weak-contacts u))
                                :quartered (count (contact/contacts-not-contacted-for-days ibc 90))
                                :strongest-contact (c-distiller/distill (last contacts) u ibc)
                                :weakest-contact (c-distiller/distill (first contacts) u ibc)}))))

(defn interaction-stats [u]
  (d-core/run-in-tz-offset (:user/login-tz u)
                           (let [contacts (sort-by contact/contact-score (contact/person-contacts u))
                                 ibc (interaction/ibc u contacts)
                                 interactions (interaction/interactions-from-ibc ibc)]
                             {:best-week-interaction-count
                              (s-int-dist/best-week-interaction-count interactions)
                              :weekly-average
                              (s-int-dist/weekly-average interactions)
                              :all-week-interaction-count
                              (interaction/all-interaction-count-in-the-past ibc 7)
                              :interaction-daily-counts
                              (interaction/daily-counts-for-network ibc)})))