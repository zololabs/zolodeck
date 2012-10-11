(ns zolo.setup.datomic-setup
  (:use [zolo.setup.config :as conf]
        [zolodeck.demonic.core :as demonic]
        [zolo.setup.datomic-schema :only [SCHEMA-TX] :as datomic-setup]
        zolodeck.utils.clojure))

(defrunonce init-datomic []
  (->> @datomic-setup/SCHEMA-TX
       (demonic/init-db (conf/datomic-db-name))))

(defn reset []
  (demonic/reset-db (conf/datomic-db-name) @datomic-setup/SCHEMA-TX)
  "OK")