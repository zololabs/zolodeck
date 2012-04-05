(ns zolo.utils.maps
  (:use zolo.utils.debug)
  (:require [clojure.walk :as walk]))

(defn transform-vals-with [a-map transform-fn]
  (apply merge (map (fn [[k v]] {k (transform-fn k v)}) a-map)))

(def stringify-keys walk/stringify-keys)

(defn stringify-vals [a-map]
  (transform-vals-with a-map (fn [k s] (str s))))

(defn stringify-map [a-map]
  (-> a-map
      walk/stringify-keys
      stringify-vals))