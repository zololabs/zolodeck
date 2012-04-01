(ns zolo.setup.config
  (:use zolo.utils.clojure))

(declare CONFIG-MAP ENV)

(defn load-config [config-file env]
  (def ENV env)
  (def CONFIG-MAP (load-string (slurp config-file))))

(defn production-mode? []
  (= :production ENV))

(defn datomic-db-name [] 
  (get-in CONFIG-MAP [ENV :datomic-db]))

(defn config-folder []
  (-> (System/getenv)
      (.get "ZOLODECK_HOME")
      (str "/config")))

(defrunonce setup-config []
  (let [config-file (str (config-folder) "/zolo.conf")
        env (keyword (.get (System/getenv) "ZOLODECK_ENV"))]
    (load-config config-file env)))

(setup-config)