(ns zolo.setup.datomic-setup
  (:use [zolo.setup.config :as conf]
        [zolodeck.demonic.core :as demonic]
        [slingshot.slingshot :only [throw+]]
        [zolo.setup.datomic-schema :only [SCHEMA-TX] :as datomic-setup]
        zolodeck.utils.clojure))

(defn assert-numbered-migrations [numbers schema]
  (doseq [n numbers]
    (if (nil? (schema n))
      (throw+ {:severity :fatal :type :datomic}
              (str "Missing datomic schema number: " n))))
  schema)

(defn validate-schema [schema]
  (-> schema 
      keys
      count
      range
      (assert-numbered-migrations schema)))

(defn validated-schema-tx [schema-tx-map]
  (->> schema-tx-map
       validate-schema
       vals))

(defrunonce init-datomic []
  (->> datomic-setup/SCHEMA-TX
       validated-schema-tx
       (demonic/init-db (conf/datomic-db-name))))

(init-datomic)