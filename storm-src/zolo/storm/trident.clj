(ns zolo.storm.trident
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        backtype.storm.config
        zolo.storm.utils)
  (:require [zolo.utils.logger :as logger])
  (:import [storm.trident TridentTopology]
           [storm.trident.testing TuplifyArgs MemoryMapState$Factory]
           [backtype.storm.tuple Fields]
           [backtype.storm StormSubmitter LocalCluster LocalDRPC]
           [storm.trident.operation.builtin Count]
           [zolo.storm.fns PrintVals UpdateContacts]))

(defn topology [drpc]
  (let [t (TridentTopology.)]
    (-> t
        (.newDRPCStream "refresh" drpc)
        (.each (fields "args") (UpdateContacts.) (fields "contact-guid"))
        ;(.each (fields "contact-guid") (PrintVals.) (fields "contact-guid"))
        (.parallelismHint 4))
    (.build t)))

(defn run-local! [topology]
  (future
    (let [cluster (LocalCluster.)]
      (logger/trace "Submitting topology...")
      (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} topology)
      (pause "Waiting..." 20000)
      cluster)))