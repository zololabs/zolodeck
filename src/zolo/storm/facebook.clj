(ns zolo.storm.facebook
  (:gen-class)
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
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

(defn refresh-guids-to-process []
  (logger/info "Finding Refresh GUIDS to process...")
  (demonic/in-demarcation
   (->> (user/find-all-users-for-refreshes)
        (remove recently-created-or-updated)
        (domap #(str (:user/guid %))))))

;; (defn new-guids-to-process []
;;   (logger/info "Finding New GUIDS to process...")
;;   (demonic/in-demarcation
;;    (->> (user/find-all-users-for-refreshes)
;;         (filter is-brand-new-user?)
;;         (domap #(str (:user/guid %))))))

(defn init-refresh-guids [guids-atom]
  (logger/info "InitRefreshGuids...")  
  (reset! guids-atom (refresh-guids-to-process))
  (if (empty? @guids-atom)
    (do
      (short-pause "Waiting for stale users..." STALE-USERS-WAIT)
      (recur guids-atom))
    guids-atom))

(defn pop-guid [guids-atom]
  (let [f (first @guids-atom)]
    (swap! guids-atom rest)
    f))

(defn next-refresh-guid [guids-atom]
  (if (empty? @guids-atom)
    (do
      (short-pause "Completed one pass of REFRESH GUIDS... now waiting..." STALE-USERS-WAIT)
      (recur (init-refresh-guids guids-atom)))
    (pop-guid guids-atom)))

(defspout refresh-user-spout ["user-guid"]
  [conf context collector]
  (let [guids (atom nil)]
    (init-refresh-guids guids)
    (spout
     (nextTuple []
                (let [n (next-refresh-guid guids)]
                  (logger/info "RefreshUserSpout emitting GUID:" n)
                  (emit-spout! collector [n])))
     (ack [id]))))

;; (defn init-new-guids [guids-atom]
;;   (logger/info "InitNewGuids...")
;;   (reset! guids-atom (new-guids-to-process))
;;   (if (empty? @guids-atom)
;;     (do
;;       (short-pause "Waiting for new users..." NEW-USER-WAIT)
;;       (recur guids-atom))
;;     guids-atom))

;; (defn next-new-guid [guids-atom]
;;   (if (empty? @guids-atom)
;;     (do
;;       (short-pause "Completed one pass of NEW GUIDS... now waiting..." NEW-USER-WAIT)
;;       (recur (init-new-guids guids-atom)))
;;     (pop-guid guids-atom)))

;; (defspout new-user-spout ["user-guid"]
;;   [conf context collector]
;;   (let [guids (atom nil)]
;;     (init-new-guids guids)
;;     (spout
;;      (nextTuple []
;;                 (let [n (next-new-guid guids)]
;;                   (logger/info "NewUserSpout emitting GUID:" n)
;;                   (emit-spout! collector [n])))
;;      (ack [id]))))

(defspout new-user-tx-spout ["user-guid"]
  [conf context collector]
  (let [trq (demonic/transactions-report-queue)]
    (spout
     (nextTuple []
                (let [tx-report (.poll trq)]
                  (if-not tx-report
                    (short-pause "No tx reports" 10)
                    (let [guid (new-user-in-tx-report tx-report)]
                      (if (empty? guid)
                        (short-pause "Not a new user tx" 10)
                        (do
                          (logger/trace "Emitting NEW user guid" guid)
                          (emit-spout! collector [guid])))))))
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
       ))
    (catch Exception e
      (logger/error e "Exception in bolt! Occured while processing tuple:" tuple))))

(defn fb-topology []
  (topology
   {"1" (spout-spec refresh-user-spout)
    "2" (spout-spec new-user-tx-spout)}
   {"3" (bolt-spec {"1" :shuffle
                    "2" :shuffle}
                   process-user
                   ;:p 2
                   )}))

(defn run-local! [millis]
  (future
    (let [cluster (LocalCluster.)]
      (logger/trace "Submitting topology...")
      (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))
      (short-pause "Running topology for millis:" millis)
      (logger/trace "Shutting down cluster!")
      (.shutdown cluster))))

(defn run-local-forever! []
  (setup-dummies)
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