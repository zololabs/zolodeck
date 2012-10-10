(ns zolo.storm.facebook
  (:use zolodeck.utils.debug
        backtype.storm.clojure
        backtype.storm.config)
  (:require [zolo.domain.user :as user])
  (:import [backtype.storm StormSubmitter LocalCluster]))

(defspout user-spout ["user-guid"]
  [conf context collector]
  (let [guids [1 2 3 4 5]]
    (spout
     (nextTuple []
                (Thread/sleep 1000)
                (print-vals "Facebook spout emitting...")
                (emit-spout! collector [(str (rand-nth guids))]))
     (ack [id]))))

(defbolt process-user [] [tuple collector]
  (let [guid (.getStringByField tuple "user-guid")
        u (print-vals "Getting user with guid:" guid)]
    (print-vals "Processing user:" u)
    (emit-bolt! collector [u] :anchor tuple)))

(defn fb-topology []
  (topology
   {"1" (spout-spec user-spout)}
   {"2" (bolt-spec {"1" :shuffle}
                   process-user)}))

(defn run-local! []
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))
    (Thread/sleep 10000)
    (print-vals "Shutting down cluster!")
    (.shutdown cluster)))