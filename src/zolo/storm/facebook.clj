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
            [zolo.domain.user-identity :as user-identity]            
            [zolo.utils.logger :as logger]
            [zolodeck.utils.clojure :as clj]
            [clojure.tools.cli :as cli]
            [zolo.social.bootstrap])
  (:import [backtype.storm StormSubmitter LocalCluster]
           [storm.trident TridentTopology]
           ;[zolo.storm.fns PrintVals UpdateContacts UpdateMessages]
           ))

(defn refresh-guids-to-process []
  ;;(logger/info "Finding Refresh GUIDS to process...")
  (demonic/in-demarcation
   (->> (user/find-all-users-for-refreshes)
        (remove recently-created-or-updated)
        (domap #(str (:user/guid %))))))

(defn init-refresh-guids [guids-atom]
  ;;(logger/info "InitRefreshGuids...")  
  (reset! guids-atom (refresh-guids-to-process))
  ;(logger/trace "Number of Refresh GUIDS:" (count @guids-at))
  (if (empty? @guids-atom)
    (short-pause "Waiting for stale users..."))
  nil)

(defn pop-guid [guids-atom]
  (let [f (first @guids-atom)]
    (swap! guids-atom rest)
    f))

(defn next-refresh-guid [guids-atom]
  (if (empty? @guids-atom)
    (do
      (short-pause "Completed one pass of REFRESH GUIDS... now waiting...")
      (init-refresh-guids guids-atom)
      nil)
    (pop-guid guids-atom)))

(defspout refresh-user-spout ["user-guid"]
  [conf context collector]
  (logger/trace "RefreshSpout, initializing Datomic...")  
  (datomic/init-connection)
  (let [guids (atom nil)]
    (spout
     (nextTuple []
                (when-let [n (next-refresh-guid guids)]
                  (demonic/in-demarcation
                   (let [u (user/find-by-guid-string n)]
                     (when (user-identity/fb-permissions-granted? u)
                       (user/stamp-refresh-start u)
                       (logger/info "RefreshUserSpout emitting GUID:" n " for " (:user/first-name u) " " (:user/last-name u))
                       (emit-spout! collector [n]))))))
     (ack [id]))))

(defn emit-new-or-perm-user [guid new-or-perm collector]
  (demonic/in-demarcation
   (let [u (user/find-by-guid-string guid)]
     (user/stamp-refresh-start u)
     (logger/trace "NewPermSpout emitting " new-or-perm " user guid" guid " for " (:user/first-name u) " " (:user/last-name u))))
  (emit-spout! collector [guid]))

(defspout new-user-tx-spout ["user-guid"]
  [conf context collector]
  (logger/trace "NewUserSpout, initializing Datomic...")    
  (datomic/init-connection)
  (let [trq (demonic/transactions-report-queue)]    
    (spout
     (nextTuple []
                (let [tx-report (.poll trq)]
                  (if-not tx-report
                    (short-pause "No tx reports")
                    (let [new-guid (new-user-in-tx-report tx-report)
                          perm-guid (permissions-granted-in-tx-report tx-report)]
                      (condp = [(not (empty? new-guid)) (not (empty? perm-guid))] 
                        [true true]   (emit-new-or-perm-user new-guid :NEW collector)
                        [true false]  (short-log (str "Insufficient permissions given for: " new-guid))
                        [false true]  (emit-new-or-perm-user perm-guid :PERMISSION collector)
                        [false false] (short-pause "Not a NewUser TX or a Perm TX")
                       )
                      ))))
     (ack [id]))))

(defbolt process-user [] [tuple collector]
  (try
    (datomic/init-connection)
    (demonic/in-demarcation
     (let [guid (.getStringByField tuple "user-guid")
           u (user/find-by-guid-string guid)]
       (logger/info "Processing user:" (:user/first-name u))
       (demonic/in-demarcation
        (user/refresh-user-data u))
       (demonic/in-demarcation
        (user/refresh-user-scores u))))
    (catch Exception e
      (logger/error e "Exception in bolt! Occured while processing tuple:" tuple))))

(defn fb-topology []
  (topology
   {"1" (spout-spec refresh-user-spout)
    "2" (spout-spec new-user-tx-spout)}
   {"3" (bolt-spec {"1" :shuffle
                    "2" :shuffle}
                   process-user
                   :p 2)}))

;; (defn fb-trident []
;;   (let [t (TridentTopology.)]
;;     (-> t
;;         (.newStream "refresh-spout" refresh-user-spout)
;;         (.each (fields "user-guid") (UpdateContacts.) (fields "contact-guid"))
;;         (.each (fields "contact-guid") (UpdateMessages.) (fields "ignored")))
;;     (.build t)))

(defn run-local! [millis]
  (future
    (datomic/init-connection)
    (let [cluster (LocalCluster.)]
      (logger/trace "Submitting topology...")
      (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))
      (pause "Running topology for millis:"millis)
      (logger/trace "Shutting down cluster!")
      ;;(.shutdown cluster)
      )))

(defn run-local-forever! []
  (let [cluster (LocalCluster.)]
    (logger/trace "Submitting topology...")
    (.submitTopology cluster "facebook" {TOPOLOGY-DEBUG true} (fb-topology))))

(defn process-args [args]
  (cli/cli args
           ["-e"  "--env" "development/staging/production" :default "development" :parse-fn #(.toLowerCase %)]
           ["-h" "--help" "Show help" :default false :flag true]))

;;TODO Make this the entry point for running both in local and remote mode

(defn -main [& cl-args]
  (StormSubmitter/submitTopology "facebook" {TOPOLOGY-DEBUG true} (fb-topology)))