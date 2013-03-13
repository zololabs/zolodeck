(ns zolo.core1
  (:gen-class)
  (:use zolodeck.utils.debug
        compojure.core
        ring.adapter.jetty
        ring.middleware.json-params)
  (:require  [clojure.tools.cli :as cli]
             [zolo.utils.logger :as logger]
             [zolo.setup.config :as config]
            ;; [zolo.storm.facebook :as fb]
             [zolo.web :as web]
             [compojure.route :as route]
             [compojure.handler :as handler]
             [zolodeck.demonic.core :as demonic]
             [ring.util.response :as response]
             [zolo.api.user-api :as user-api]
             [zolo.api.suggestion-set-api :as ss-api]))

(defroutes application-routes
  (route/resources "/")

  ;;Users
  (POST "/users" {params :params} (web/json-response (user-api/new-user params)))

  ;;TODO Need to fix this for REST
  (GET "/users" {params :params} (-> (user-api/find-users params)
                                     web/json-response))

  ;;TODO Just loging in the user it is not Updating the User 
  (PUT "/users/:guid" [guid :as {params :params}] (web/json-response (user-api/update-user guid params)))

  ;;TODO move this to its own routes
  (GET "/users/:user-id/suggestion_sets/:name" [user-id name] (web/json-response (ss-api/find-suggestion-set user-id name)))

  (GET "/users/:user-id/suggestion_sets" [user-id :as {params :params}] (web/json-response (ss-api/find-suggestion-sets user-id params))))

(def app
  (web/wrap-request-binding  
   (web/wrap-options
    (-> application-routes        
        ;;web/wrap-user-info-logging
        handler/api
        wrap-json-params
        web/wrap-accept-header-validation
        web/wrap-error-handling
        demonic/wrap-demarcation
        web/wrap-request-logging))))

(defn start-api
  ([]
     (start-api 4000))
  ([port]
     (zolo.setup.datomic-setup/init-datomic)
     (run-jetty (var app) {:port port
                           :join? false})))  

(defn start-storm []
  (zolo.setup.datomic-setup/init-datomic)
  ;; (logger/with-logging-context {:env (config/environment)}
  ;;   (fb/run-local-forever!))
  )

(defn process-args [args]
  (cli/cli args
           ["-s"  "--service" "storm/api" :default "api" :parse-fn #(keyword (.toLowerCase %))]
           ["-p" "--port" "Listen on this port" :default 4000  :parse-fn #(Integer. %)] 
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
        :api (start-api (:port options))
        :default (throw "Invalid Service :" (:s options)))))
