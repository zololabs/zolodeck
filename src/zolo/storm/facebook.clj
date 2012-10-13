(ns zolo.storm.facebook
  (:use zolodeck.utils.debug        
        backtype.storm.clojure
        backtype.storm.config)
  (:require [zolo.setup.datomic-setup :as datomic]
            [zolodeck.demonic.core :as demonic]
            [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolodeck.utils.calendar :as zolo-cal])
  (:import [backtype.storm StormSubmitter LocalCluster]))

(def UPDATE-FREQUENCE-MILLIS (* 1000 60 60)) ;; 1 HOUR

(defn recently-updated [[guid last-updated]]
  (let [now (zolo-cal/now)
        elapsed (- now (.getTime last-updated))
        recent? (< elapsed UPDATE-FREQUENCE-MILLIS)]
    (logger/trace "User:" guid ", recently updated:" recent?)
    recent?))

(defn user-guids-to-process []
  (print-vals "Finding User GUIDS to process...")
  (demonic/in-demarcation
   (->> (user/find-all-user-guids-and-last-updated)
        (remove recently-updated)
        (map first)
        (map str))))

(defn init-guids [guids-atom]
  (print-vals "InitGuids...")  
  (reset! guids-atom (user-guids-to-process)))

(defn next-guid [guids-atom]
  (when (empty? @guids-atom)
    (print-vals "guids is empty... calling init-guids")
    (init-guids guids-atom)
    (when (empty? @guids-atom)
      (logger/trace "Waiting 10s for stale users...")
      (Thread/sleep 10000)))
  (let [f (first @guids-atom)]
    (swap! guids-atom rest)
    f))

(defspout user-spout ["user-guid"]
  [conf context collector]
  (let [guids (atom nil)]
    (init-guids guids)
    (spout
     (nextTuple []
                (Thread/sleep 1000)
                (print-vals "Facebook spout emitting...")
                (let [n (next-guid guids)]
                  (print-vals "Next GUID is:" n)
                  (emit-spout! collector [n])))
     (ack [id]))))

(defbolt process-user [] [tuple collector]
  (demonic/in-demarcation
   (let [guid (.getStringByField tuple "user-guid")
         u (user/find-by-guid-string guid)]
     (print-vals "Processing user:" (:user/first-name u))
     (demonic/in-demarcation
      (user/refresh-user-data u))
     ;(emit-bolt! collector [u] :anchor tuple)
     )))

(defn fb-topology []
  (topology
   {"1" (spout-spec user-spout)}
   {"2" (bolt-spec {"1" :shuffle}
                   process-user)}))

(defn run-local! []
  (let [cluster (LocalCluster.)]
    (print-vals "Submitting topology...")
    (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))
    (Thread/sleep 60000)
    (print-vals "Shutting down cluster!")
    (.shutdown cluster)))

(defn run-local-forever! []
  (let [cluster (LocalCluster.)]
    (print-vals "Submitting topology...")
    (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))))

;; (require '[zolo.storm.facebook :as fb]) (fb/run-local!)