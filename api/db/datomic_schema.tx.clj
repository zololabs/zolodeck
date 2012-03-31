(use '[datomic.api :only [q db] :as db])

(def schema {
     001 {:db/id #db/id[:db.part/db]
          :db/ident :user/first-name
          :db/valueType :db.type/string
          :db/cardinality :db.cardinality/one
          :db/fulltext true
          :db/doc "A user's first name"
          :db.install/_attribute :db.part/db}

     002 {:db/id #db/id[:db.part/db]
          :db/ident :user/last-name
          :db/valueType :db.type/string
          :db/cardinality :db.cardinality/one
          :db/fulltext true
          :db/doc "A user's last name"
          :db.install/_attribute :db.part/db}             
})