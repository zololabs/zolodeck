(ns zolo.storm.core
  (:gen-class)
  (:use zolo.utils.debug)
  (:require  [clojure.tools.cli :as cli]
             [zolo.utils.logger :as logger]
             [zolo.setup.config :as config]
             [zolo.social.bootstrap]
             [zolo.storm.facebook :as fb]
             [zolodeck.demonic.core :as demonic]))

(defn start-storm []
  (zolo.setup.datomic-setup/init-datomic)
  (logger/with-logging-context {:env (config/environment)}
    (fb/run-local-forever!)))

(defn process-args [args]
  (cli/cli args
           ["-s"  "--service" "storm" :default "storm" :parse-fn #(keyword (.toLowerCase %))]
           ["-h" "--help" "Show help" :default false :flag true]))

(defn -main [& cl-args]
  (config/setup-config)
  (print-vals "CL Args :" cl-args)
  (let [[options args banner] (process-args cl-args)]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (condp = (:service (print-vals "Options :" options))
        :storm (start-storm)
        :default (throw "Invalid Service :" (:s options)))))
