(ns zolo.store.contact-store
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.demonic.core :as demonic]))

;;TODO test
(defn find-by-guid [guid]
  (when guid
    (->> (if (string? guid) (java.util.UUID/fromString guid) guid)
         (demonic/run-query '[:find ?c :in $ ?guid :where [?c :contact/guid ?guid]])
         ffirst
         demonic/load-entity)))