(ns zolo.domain.suggestion-set.strategy.random
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.interaction :as interaction]
            [zolo.domain.contact :as contact]))

(defn compute [u]
  (let [ibc (interaction/ibc u (contact/person-contacts u))]
    (->> u
         contact/person-contacts         
         (remove contact/is-muted?)
         (remove #(contact/is-contacted-today? % ibc))
         (take-unique-randomely 5))))