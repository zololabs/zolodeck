(ns zolo.infra.datomic
  (:use [datomic.api :only [q db] :as db]
        [slingshot.slingshot :only [throw+]]
        [zolo.setup.config :only [datomic-db-name] :as conf]
        [zolo.setup.datomic-schema :only [SCHEMA-TX] :as datomic-setup]
        zolo.utils.debug))

(declare CONN assert-numbered-migrations init-db)

(def ^:dynamic TX-DATA)

(def ^:dynamic DATOMIC-DB)

(defn datomic-fixture [test-fn]
  (init-db)
  (test-fn))

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

;; (defn ^:dynamic run-transaction [tx-data]
;;   @(db/transact CONN tx-data))

(defn run-transaction [tx-data]
  (swap! TX-DATA concat tx-data)
  (swap! DATOMIC-DB db/with tx-data))

(defn commit-transactions []
  @(db/transact CONN @TX-DATA))

(defn insert-new [a-map]
  (-> {:db/id #db/id[:db.part/user]}
      (merge a-map)
      vector
      run-transaction))

(defn run-query [query & extra-inputs]
  (apply q query @DATOMIC-DB extra-inputs))

(defn load-entity [eid]
  (db/entity (get-db) eid))

(defn run-in-datomic-demarcation [thunk]
  (binding [TX-DATA (atom [])
            DATOMIC-DB (atom (get-db))]
    (thunk)
    (commit-transactions)))

(defmacro in-datomic-demarcation [& body]
  `(run-in-datomic-demarcation (fn [] ~@body)))

