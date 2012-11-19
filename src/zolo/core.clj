(ns zolo.core
  (:gen-class)
  (:use zolodeck.utils.debug
        compojure.core
        ring.adapter.jetty
        ring.middleware.params
        ring.middleware.cookies
        ring.middleware.keyword-params
        ring.middleware.json-params
        ring.middleware.nested-params
        ring.middleware.cookies
        [sandbar.auth]
        [sandbar.validation]
        [sandbar.stateful-session :only [wrap-stateful-session]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.tools.cli :as cli]
            [zolodeck.demonic.core :as demonic]
            [zolo.web.auth :as auth]
            [zolo.social.bootstrap]
            [zolo.social.core :as social]
            [zolo.api.user-api :as user-api]
            [zolo.utils.logger :as logger]
            [zolo.web :as web]
            [zolo.storm.facebook :as fb]))

(def security-policy
  [#"/permission-denied*" :any
   #"/users" :any
   #"/users.*" :user
   #".*" :user])

(defroutes application-routes
  (route/resources "/")

  ;;---- USER
  (POST "/users" {params :params cookies :cookies} (web/json-response (user-api/signin-user params cookies)))  
  (GET "/users/:id" [id] (web/json-response (current-user)))

  (POST "/messages" {params :params} (web/json-response (user-api/send-message params)))
  
  ;;---- User Stats
  (GET "/user-stats" {params :params} (web/json-response (user-api/stats params)))
  
  ;;---- GENERAL
  (GET "/permission-denied*" []  (web/json-response {:error "Permission Denied"} 403))

  (route/not-found "Page not found"))

(def app
  (web/wrap-request-binding  
   (web/wrap-options
    (-> application-routes
        web/wrap-user-info-logging
        handler/api          
        wrap-json-params
        (with-security security-policy auth/authenticator)
        web/wrap-accept-header-validation
        wrap-stateful-session
        web/wrap-error-handling
        demonic/wrap-demarcation
        web/wrap-request-logging
        wrap-cookies        
        ))))


(defn start-api []
  (zolo.setup.datomic-setup/init-datomic)
  (run-jetty (var app) {:port 4000
                        :join? false}))

(defn start-storm []
  (zolo.setup.datomic-setup/init-datomic)
  (fb/run-local-forever!))

(defn process-args [args]
  (cli/cli args
           ["-s"  "--service" "storm/api" :default "api" :parse-fn #(keyword (.toLowerCase %))]
           ["-h" "--help" "Show help" :default false :flag true]))

(defn -main [& cl-args]
  (print-vals "CL Args :" cl-args)
  (print-vals "Starting in zolodeck ENV : " (zolo.setup.config/get-env-var "ZOLODECK_ENV"))
  (let [[options args banner] (process-args cl-args)]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (condp = (:service (print-vals "Options :" options))
        :storm (start-storm)
        :api (start-api)
        :default (throw "Invalid Service :" (:s options)))))

