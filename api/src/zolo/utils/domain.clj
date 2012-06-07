(ns zolo.utils.domain
  (:require [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.demonic.schema :as schema]))

(defn force-schema-attrib [attrib value]
  (cond
   (and (schema/is-string? attrib) (not (string? value))) (str value)
   :else value))

(defn force-schema-types [a-map]
  (zolo-maps/transform-vals-with a-map force-schema-attrib))

(defn group-by-attrib [objects attrib]
  (-> (group-by attrib objects)
      (zolo-maps/transform-vals-with (fn [_ v] (first v)))))

