(ns zolo.utils.countries
  (:use zolo.utils.clojure)
  (:require [zolo.utils.maps :as maps]
            [country.list :as wcl]))

(defrunonce init []
  (let [en (wcl/parse-countries :en)
        grouped (group-by :country-code en)
        grouped (maps/transform-vals-with grouped (fn [_ v] (first v)))]
    (def EN grouped)))

(init)

(defn country-name-for [country-code]
  (if country-code
    (get-in EN [(.toUpperCase country-code) :full-name])))