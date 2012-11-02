(ns zolo.storm.facebook
  (:gen-class)
  (:use zolodeck.utils.debug        
        backtype.storm.clojure
        backtype.storm.config
        zolo.storm.utils)
  (:require [zolo.setup.datomic-setup :as datomic]
            [zolo.setup.config :as conf]
            [zolodeck.demonic.core :as demonic]
            [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolodeck.utils.clojure :as clj]
            [clojure.tools.cli :as cli])
  (:import [backtype.storm StormSubmitter LocalCluster]))

(defn user-guids-to-process []
  (logger/info "Finding User GUIDS to process...")
  (demonic/in-demarcation
   (->> (user/find-all-users-for-refreshes)
        (remove recently-updated)
        (map :user/guid)
        (map str))))

(defn init-guids [guids-atom]
  (logger/info "InitGuids...")  
  (reset! guids-atom (user-guids-to-process))
  (if (empty? @guids-atom)
    (do
      (pause "Waiting for stale users..." STALE-USERS-WAIT)
      (recur guids-atom))
    guids-atom))

(defn pop-guid [guids-atom]
  (let [f (first @guids-atom)]
    (swap! guids-atom rest)
    f))

(defn next-guid [guids-atom]
  (if (empty? @guids-atom)
    (do
      (pause "Completed one pass of all GUIDS... now waiting..." USER-UPDATE-WAIT)
      (recur (init-guids guids-atom)))
    (pop-guid guids-atom)))

(defspout user-spout ["user-guid"]
  [conf context collector]
  (let [guids (atom nil)]
    (init-guids guids)
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
        (user/refresh-user-data u))))
    (catch Exception e
      (logger/error e "Exception in bolt! Occured while processing tuple:" tuple))))

(defn fb-topology []
  (topology
   {"1" (spout-spec user-spout)}
   {"2" (bolt-spec {"1" :shuffle}
                   process-user
                   ;:p 2
                   )}))

(defn run-local! [millis]
  (future
    (let [cluster (LocalCluster.)]
      (logger/trace "Submitting topology...")
      (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))
      (pause "Running topology for " millis " millis...")
      (logger/trace "Shutting down cluster!")
      (.shutdown cluster))))

(defn run-local-forever! []
  (print-vals "UserGuidsToProcess:" (user-guids-to-process))
  (let [cluster (LocalCluster.)]
    (logger/trace "Submitting topology...")
    (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))))


(defn process-args [args]
  (cli/cli args
           ["-e"  "--env" "development/staging/production" :default "development" :parse-fn #(keyword (.toLowerCase %))]
           ["-h" "--help" "Show help" :default false :flag true]))

;;TODO Make this the entry point for running both in local and remote mode

(defn -main [& cl-args]
  (print-vals "CL Args :" cl-args)
  (let [[options args banner] (process-args cl-args)]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (let [env (:env options)
          cluster (StormSubmitter. )]
      (System/setProperty "ZOLODECK_ENV" env)
      (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology)))))