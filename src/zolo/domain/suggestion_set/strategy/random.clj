(ns zolo.domain.suggestion-set.strategy.random
  (:use zolo.utils.debug)
  (:require [zolo.domain.interaction :as interaction]
            [zolo.domain.contact :as contact]))

(defn compute [u]
  (let [ibc (interaction/ibc u)]
    (->> u
         :user/contacts
         (remove #(contact/is-contacted-today? % ibc))
         (take 5))))