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

(defn- kv-updater-for-key [key-tester key-updater [k v]]
  (if-let [new-key (key-updater k)]
    (if (key-tester k)
      {new-key v}
      {k v})))

;;TODO need to add tests

(defn update-map-keys [m key-tester key-updater]
  (apply merge (map (partial kv-updater-for-key key-tester key-updater) m)))

(defn update-all-map-keys [m key-updater]
  (update-map-keys m (constantly true) key-updater))


(defn keywordize-string [s]
  (-> s
      name      
      (clojure.string/replace "_" "-")
      keyword))

(defn keywordize-map [m]
  (update-all-map-keys m keywordize-string))
