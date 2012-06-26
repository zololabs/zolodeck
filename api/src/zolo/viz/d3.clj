(ns zolo.viz.d3
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.zolo-graph :as zg]))

(defn d3-node [name group]
  {"name" (.toString name)
   "group" group})

(defn d3-link [target value]
  {"source" 0
   "target" target
   "value" value})

(defn d3-nodes [zg]
  (reduce (fn [acc [c-id c]]
            (if (zg/has-score? zg c-id)
              (conj acc (d3-node c-id 1))
              acc))
          [(d3-node (zg/user-guid zg) 1000)]
          (zg/contacts zg)))

(defn add-d3-link [links zg c-id target]
  (if (zg/has-score? zg c-id)
    (conj links (d3-link target (zg/score-value zg c-id)))
    links))

(defn d3-link-next-target [zg c-id current-target]
  (if (zg/has-score? zg c-id) 
    (inc current-target)
    current-target))

(defn d3-links [zg]
  (loop [links []
         c-ids (zg/contact-guids zg)
         target 1]
    (if (not (empty? c-ids))
      (recur (add-d3-link links zg (first c-ids) target)
             (rest c-ids) 
             (d3-link-next-target zg (first c-ids) target))
      links)))

(defn format-for-d3 [zg]
  {"nodes" (d3-nodes zg)
   "links" (d3-links zg)})
