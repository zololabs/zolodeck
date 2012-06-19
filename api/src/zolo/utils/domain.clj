(ns zolo.utils.domain
  (:require [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.demonic.schema :as schema]))

(defn force-schema-attrib [attrib value]
  (cond
   (and (schema/is-string? attrib) (not (string? value))) (str value)
   :else value))

(defn force-schema-types [a-map]
  (zolo-maps/transform-vals-with a-map force-schema-attrib))

;;TODO Add test for this function
(defn group-first-by [attrib objects]
  (-> (group-by attrib objects)
      (zolo-maps/transform-vals-with (fn [_ v] (first v)))))

(defn update-fresh-entities-with-db-id [existing-entities fresh-entities group-by-fn]
  (if (empty? existing-entities)
    fresh-entities
    (let [existing-entities-grouped (group-first-by group-by-fn existing-entities)
          fresh-entities-grouped (group-first-by group-by-fn fresh-entities)]
      (map
       (fn [[obj-id fresh-obj]]
         (assoc fresh-obj :db/id (:db/id (existing-entities-grouped obj-id))))
       fresh-entities-grouped))))