(ns zolo.service.distiller.suggestion-set
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.suggestion-set :as s-set]
            [zolo.service.distiller.contact :as c-distiller]))

(defn- contact-info [c ibc]
  (-> c
      (c-distiller/distill ibc)
      (assoc :contact/reason-to-connect (s-set/reason-for-suggesting c ibc))))

(defn distill [ss ibc]
  (when ss
    {:suggestion-set/name (:suggestion-set/name ss)
     :suggestion-set/contacts (if (nil? ibc)
                                []
                                (domap #(contact-info % ibc) (:suggestion-set/contacts ss)))}))