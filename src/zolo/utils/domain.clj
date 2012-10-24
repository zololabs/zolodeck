(ns zolo.utils.domain
  (:use [zolodeck.utils.debug])
  (:require [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.demonic.schema :as schema]))

(defn force-schema-attrib [attrib value]
  (if value
    (cond
     (and (schema/is-string? attrib) (not (string? value))) (str value)
     (and (schema/is-strings? attrib) (not (every? string? value))) (map str value)
     (and (schema/is-long? attrib) (not (number? value))) (Long/parseLong value)
     :else value)
    value))

(defn force-schema-types [a-map]
  (-> a-map
      (zolo-maps/transform-vals-with force-schema-attrib)
      (zolo-maps/select-keys-if (fn [k v] (not (nil? v))))))

(defn group-first-by [attrib objects]
  (-> (group-by attrib objects)
      (zolo-maps/transform-vals-with (fn [_ v] (first v)))))

(defn update-fresh-entities-with-db-id [existing-entities fresh-entities group-by-fn guid-key]
  (if (empty? existing-entities)
    fresh-entities
    (let [existing-entities-grouped (group-first-by group-by-fn existing-entities)
          fresh-entities-grouped (group-first-by group-by-fn fresh-entities)]
      (map
       (fn [[obj-id fresh-obj]]
         (-> fresh-obj
           (assoc :db/id (:db/id (existing-entities-grouped obj-id)))
           (assoc guid-key (guid-key (existing-entities-grouped obj-id)))))
       fresh-entities-grouped))))