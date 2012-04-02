(ns zolo.infra.datomic-helper
  (:use [datomic.api :only [q db] :as db]
        [slingshot.slingshot :only [throw+]]
        [zolo.setup.config :only [datomic-db-name] :as conf]
        [zolo.setup.datomic-schema :only [SCHEMA-TX] :as datomic-setup]
        zolo.utils.debug
        zolo.utils.clojure))

(declare CONN assert-numbered-migrations init-db)

(def ^:dynamic TX-DATA)
(def ^:dynamic DATOMIC-DB)
(def ^:dynamic DATOMIC-TEST false)

(defn validate-schema [schema]
  (-> schema 
      keys
      count
      range
      (assert-numbered-migrations schema)))

(defn assert-numbered-migrations [numbers schema]
  (doseq [n numbers]
    (if (nil? (schema n))
      (throw+ {:severity :fatal :type :datomic}
              (str "Missing datomic schema number: " n))))
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

(defrunonce init-db []
  (db/create-database (conf/datomic-db-name))
  (def CONN (db/connect (conf/datomic-db-name)))
  (setup-schema datomic-setup/SCHEMA-TX))

(init-db)

(defn run-transaction [tx-data]
  (swap! TX-DATA concat tx-data)
  (swap! DATOMIC-DB db/with tx-data))

(defn commit-pending-transactions []
  (if-not DATOMIC-TEST
    @(db/transact CONN @TX-DATA)))

(defn run-in-datomic-demarcation [thunk]
  (binding [TX-DATA (atom [])
            DATOMIC-DB (atom (get-db))]
    (thunk)
    (commit-pending-transactions)))

