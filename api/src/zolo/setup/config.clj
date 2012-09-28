(ns zolo.setup.config
  (:use zolodeck.utils.clojure
        zolodeck.utils.debug)
  (:require [clojure.java.io :as java-io]))

(declare CONFIG-MAP)
(declare ^:dynamic ENV)

(defn load-config [config-file env]
  (def ^:dynamic ENV env)
  (def CONFIG-MAP (load-string (slurp config-file))))

(defn production-mode? []
  (= :production ENV))

(defn datomic-db-name [] 
  (get-in CONFIG-MAP [ENV :datomic-db]))

;; TODO rename app-id, app-secret to make it FB specific
(defn app-id []
  (get-in CONFIG-MAP [ENV :app-id]))

(defn app-secret []
  (get-in CONFIG-MAP [ENV :app-secret]))

(defn gigya-key []
  (get-in CONFIG-MAP [ENV :gigya-key]))

(defn gigya-secret []
  (get-in CONFIG-MAP [ENV :gigya-secret]))

(defrunonce setup-config []
  (let [config-file (java-io/resource "zolo.clj")
        env (keyword (or (.get (System/getenv) "ZOLODECK_ENV") "development"))]
    (load-config config-file env)))

(setup-config)