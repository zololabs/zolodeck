(ns zolo.setup.datomic-setup
  (:use [zolo.setup.config :as conf]
        [zolo.demonic.core :as demonic]
        [zolo.demonic.helper :as demonic-helper]
        [zolo.setup.datomic-schema :only [SCHEMA-TX] :as datomic-setup]
        zolo.utils.clojure))

(defrunonce init-datomic []
  (->> @datomic-setup/SCHEMA-TX
       (demonic/init-db (conf/datomic-db-name))))

(defrunonce init-connection []
  (demonic-helper/setup-connection (conf/datomic-db-name)))

(defn reset []
  (demonic/reset-db (conf/datomic-db-name) @datomic-setup/SCHEMA-TX)
  "OK")