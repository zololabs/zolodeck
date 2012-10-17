(ns zolo.storm.facebook
  (:use zolodeck.utils.debug        
        backtype.storm.clojure
        backtype.storm.config)
  (:require [zolo.setup.datomic-setup :as datomic]
            [zolo.setup.config :as conf]
            [zolodeck.demonic.core :as demonic]
            [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolodeck.utils.calendar :as zolo-cal])
  (:import [backtype.storm StormSubmitter LocalCluster]))

(defn pause [msg millis]
  (logger/trace "[Sleep ms:" millis "] " msg)
  (Thread/sleep millis))

(def USER-UPDATE-WAIT (conf/user-update-wait-fb-millis)) ;; 1 HOUR

(def STALE-USERS-WAIT (conf/stale-users-wait-fb-millis)) ;; 1 MINUTE

(defn recently-updated [[guid last-updated]]
  (let [now (zolo-cal/now)
        elapsed (- now (.getTime last-updated))
        recent? (< elapsed USER-UPDATE-WAIT)]
    (logger/trace "User:" guid ", recently updated:" recent?)
    recent?))

(defn user-guids-to-process []
  (logger/info "Finding User GUIDS to process...")
  (demonic/in-demarcation
   (->> (user/find-all-user-guids-and-last-updated)
        (remove recently-updated)
        (map first)
        (map str))))

(defn init-guids [guids-atom]
  (logger/info "InitGuids...")  
  (reset! guids-atom (user-guids-to-process))
  (if (empty? @guids-atom)
    (do
      (pause "Waiting 10s for stale users..." STALE-USERS-WAIT)
      (recur guids-atom))
    guids-atom))

(defn pop-guid [guids-atom]
  (let [f (first @guids-atom)]
    (swap! guids-atom rest)
    f))

(defn next-guid [guids-atom]
  (if (empty? @guids-atom)
    (recur (init-guids guids-atom))
    (pop-guid guids-atom)))

(defspout user-spout ["user-guid"]
  [conf context collector]
  (let [guids (atom nil)]
    (spout
     (nextTuple []
                (let [n (next-guid guids)]
                  (logger/info "Facebook spout emitting GUID:" n)
                  (emit-spout! collector [n])))
     (ack [id]))))

(defbolt process-user [] [tuple collector]
  (try
    (demonic/in-demarcation
     (let [guid (.getStringByField tuple "user-guid")
           u (user/find-by-guid-string guid)]
       (logger/info "Processing user:" (:user/first-name u))
       (demonic/in-demarcation
        (user/stamp-refresh-start u))
       (demonic/in-demarcation
        (user/refresh-user-data u))
       ;(emit-bolt! collector [u] :anchor tuple)
       ))
    (catch Exception e
      (logger/error e "Exception in bolt! Occured while processing tuple:" tuple))))

(defn fb-topology []
  (topology
   {"1" (spout-spec user-spout)}
   {"2" (bolt-spec {"1" :shuffle}
                   process-user)}))

(defn run-local! [millis]
  (future
    (let [cluster (LocalCluster.)]
      (logger/trace "Submitting topology...")
      (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))
      (pause "Running topology for 1 minute" millis)
      (logger/trace "Shutting down cluster!")
      (.shutdown cluster))))

(defn run-local-forever! []
  (let [cluster (LocalCluster.)]
    (logger/trace "Submitting topology...")
    (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))))

;; (require '[zolo.storm.facebook :as fb]) (fb/run-local!)