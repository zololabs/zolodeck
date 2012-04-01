(ns zolo.setup.datomic
  (:use [datomic.api :only [q db] :as db]
        [slingshot.slingshot :only [throw+]]
        [zolo.setup.config :only [datomic-db-name] :as conf]
        [zolo.setup.datomic-schema :only [SCHEMA-TX] :as datomic-setup]
        zolo.utils.debug))

(declare CONN assert-numbered-migrations)

(defn validate-schema [schema]
  (-> schema 
      keys
      count
      range
      (assert-numbered-migrations schema)))

(defn assert-numbered-migrations [numbers schema]
  (doseq [n numbers]
    (if (nil? (schema n))
      (throw+ {:severity :fatal :type :datomic} (str "Missing datomic schema number: " n))))
  schema)

(defn setup-schema [schema-tx-map]
  (->> schema-tx-map
       validate-schema
       vals
       (apply vector)
       (db/transact CONN)
       deref))

(defn get-db []
  (db/db CONN))

(defn init-db []
  (db/delete-database (conf/datomic-db-name))
  (db/create-database (conf/datomic-db-name))
  (def CONN (db/connect (conf/datomic-db-name)))
  (setup-schema datomic-setup/SCHEMA-TX))

(defn run-transaction [tx-data]
  @(db/transact CONN tx-data))

(defn insert-new [a-map]
  (-> {:db/id #db/id[:db.part/user]}
      (merge a-map)
      vector
      run-transaction))

(defn run-query [query & extra-inputs]
  (apply q query (get-db) extra-inputs))

(defn load-entity [eid]
  (db/entity (get-db) eid))