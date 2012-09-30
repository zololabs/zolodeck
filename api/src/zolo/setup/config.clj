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

(defn fb-app-id []
  (get-in CONFIG-MAP [ENV :fb-app-id]))

(defn fb-app-secret []
  (get-in CONFIG-MAP [ENV :fb-app-secret]))

(defrunonce setup-config []
  (let [config-file (java-io/resource "zolo.clj")
        env (keyword (or (.get (System/getenv) "ZOLODECK_ENV") "development"))]
    (load-config config-file env)))

(setup-config)