(ns zolo.infra.datomic
  (:use [datomic.api :only [q db] :as db]
        zolo.infra.datomic-helper
        zolo.utils.debug))

(defmacro in-datomic-demarcation [& body]
  `(run-in-datomic-demarcation (fn [] ~@body)))

(defn wrap-datomic-demarcation [handler]
  (fn [request]
    (if-not DATOMIC-TEST
      (in-datomic-demarcation (handler request))
      (handler request))))

(defn run-query [query & extra-inputs]
  (apply q query @DATOMIC-DB extra-inputs))

(defn load-entity [eid]
  (db/entity @DATOMIC-DB eid))

(defn upsert [a-map]
  (-> {:db/id #db/id[:db.part/user]}
      (merge a-map)
      vector
      run-transaction))

(defn delete [entity-id]
  (-> [:db.fn/retractEntity entity-id]
      vector
      run-transaction))