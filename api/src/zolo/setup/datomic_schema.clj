(ns zolo.setup.datomic-schema)

(def SCHEMA-TX {
     000 {:db/id #db/id[:db.part/db]
          :db/ident :user/first-name
          :db/valueType :db.type/string
          :db/cardinality :db.cardinality/one
          :db/fulltext true
          :db/doc "A user's first name"
          :db.install/_attribute :db.part/db}

     001 {:db/id #db/id[:db.part/db]
          :db/ident :user/last-name
          :db/valueType :db.type/string
          :db/cardinality :db.cardinality/one
          :db/fulltext true
          :db/doc "A user's last name"
          :db.install/_attribute :db.part/db}

     002 {:db/id #db/id[:db.part/db]
          :db/ident :user/facebook-id
          :db/valueType :db.type/string
          :db/cardinality :db.cardinality/one
          :db/fulltext false
          :db/doc "A user's Facebook ID"
          :db.install/_attribute :db.part/db}

     003 {:db/id #db/id[:db.part/db]
          :db/ident :user/fb-auth-token
          :db/valueType :db.type/string
          :db/cardinality :db.cardinality/one
          :db/fulltext false
          :db/doc "A user's Facebook authentication token"
          :db.install/_attribute :db.part/db}             
})