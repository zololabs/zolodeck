(ns zolo.store.contact-store
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.demonic.core :as demonic]
            [zolo.demonic.helper :as dh]))

(defn find-entity-id-by-guid [guid]
  (when guid
    (->> (if (string? guid) (java.util.UUID/fromString guid) guid)
         (demonic/run-query '[:find ?c :in $ ?guid :where [?c :contact/guid ?guid]])
         ffirst)))

(defn find-by-guid [guid]
  (-> guid
      find-entity-id-by-guid
      demonic/load-entity))

(defn find-entity-by-guid [guid]
  (-> guid
      find-entity-id-by-guid
      dh/load-from-db))

(defn save [new-values]
  (-> new-values
      demonic/insert-and-reload))