(ns zolo.utils.countries
  (:use world.country.list
        zolodeck.utils.clojure)
  (:require [zolodeck.utils.maps :as maps]))

(defrunonce init []
  (let [en (parse-countries :en)
        grouped (group-by :country-code en)
        grouped (maps/transform-vals-with grouped (fn [_ v] (first v)))]
    (def EN grouped)))

(init)

(defn country-name-for [country-code]
  (get-in EN [country-code :full-name]))