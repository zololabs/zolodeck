(ns zolo.core
  (:gen-class)
  (:use zolo.utils.debug
        compojure.core
        ring.adapter.jetty
        ring.middleware.json-params)
  (:require  [clojure.tools.cli :as cli]
             [zolo.utils.logger :as logger]
             [zolo.setup.config :as config]
             [zolo.social.bootstrap]
             [zolo.web :as web]
             [compojure.route :as route]
             [compojure.handler :as handler]
             [zolo.demonic.core :as demonic]
             [ring.util.response :as response]
             [zolo.api.user-api :as user-api]
             [zolo.api.message-api :as m-api]
             [zolo.api.contact-api :as c-api]
             [zolo.api.suggestion-set-api :as ss-api]
             [zolo.api.stats-api :as s-api]
             [zolo.api.server-api :as server-api]))

(defroutes application-routes
  (route/resources "/")

  ;;Users
  (POST "/users" {params :params} (-> params user-api/new-user))

  ;;TODO Need to fix this for REST
  (GET "/users" {params :params} (-> params user-api/find-users ))

  ;;TODO Just loging in the user it is not Updating the User 
  (PUT "/users/:guid" [guid :as {params :params}] (user-api/update-user guid params))

  (GET "/users/:guid" [guid] (-> guid user-api/find-user ))

  ;;TODO move this to its own routes
  ;;(GET "/users/:user-id/suggestion_sets/:name" [user-id name] (web/json-response (ss-api/find-suggestion-set user-id name)))

  (GET "/users/:user-guid/suggestion_sets" [user-guid :as {params :params}] (ss-api/find-suggestion-sets user-guid params))

  ;;Contacts
  (GET "/users/:user-guid/contacts/:c-guid" [user-guid c-guid] (c-api/find-contact user-guid c-guid))

  (PUT "/users/:user-guid/contacts/:c-guid" [user-guid c-guid & params] (c-api/update-contact user-guid c-guid params))
  
  ;;Messages
  (POST "/users/:user-guid/contacts/:c-guid/messages" [user-guid c-guid & params] (m-api/send-message user-guid c-guid params))

  ;;Stats
  (GET "/users/:guid/contact_stats" [guid] (s-api/get-contact-stats guid))

  (GET "/users/:guid/interaction_stats" [guid] (s-api/get-interaction-stats guid))

  ;;GENERAL
  (GET "/server/status" {params :params} (server-api/status params))

  (route/not-found "Page not found"))

(def app
  (web/wrap-request-binding  
   (web/wrap-options
    (-> application-routes
        demonic/wrap-demarcation
        ;;web/wrap-user-info-logging
        handler/api
        wrap-json-params
        web/wrap-accept-header-validation
        web/wrap-jsonify
        web/wrap-error-handling
        web/wrap-request-logging))))

(defn start-api
  ([]
     (start-api 4000))
  ([port]
     (zolo.setup.datomic-setup/init-datomic)
     (run-jetty (var app) {:port port
                           :join? false})))  

(defn process-args [args]
  (cli/cli args
           ["-s"  "--service" "api" :default "api" :parse-fn #(keyword (.toLowerCase %))]
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
        :api (start-api (:port options))
        :default (throw "Invalid Service :" (:s options)))))
