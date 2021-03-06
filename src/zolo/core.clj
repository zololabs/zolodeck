(ns zolo.core
  (:gen-class)
  (:use zolo.utils.debug
        compojure.core
        ring.adapter.jetty
        ring.middleware.json-params)
  (:require  [clojure.tools.cli :as cli]
             [cemerick.friend :as friend]
             (cemerick.friend [workflows :as workflows]
                              [credentials :as creds])
             [zolo.web.auth :as zauth]
             [zolo.utils.logger :as logger]
             [zolo.setup.config :as config]
             [zolo.social.bootstrap]
             [zolo.service.bootstrap]
             [zolo.web :as web]
             [zolo.utils.web :as zweb]
             [compojure.route :as route]
             [ring.middleware.keyword-params :as kw-params-mw]
             [ring.middleware.params :as params-mw]
             [ring.middleware.nested-params :as nested-params-mw]
             [zolo.demonic.core :as demonic]
             [ring.util.response :as response]
             [zolo.api.user-api :as user-api]
             [zolo.api.message-api :as m-api]
             [zolo.api.contact-api :as c-api]
             [zolo.api.suggestion-set-api :as ss-api]
             [zolo.api.stats-api :as s-api]
             [zolo.api.thread-api :as t-api]
             [zolo.api.server-api :as server-api]
             [zolo.api.context-io-api :as cio-api]
             [zolo.metrics-ring.expose :as metrics-json]
             [zolo.metrics-ring.instrument :as metrics-inst]
             [zolo.service.metrics-collector :as metrics-collector]))

(derive :zolo.roles/owner :zolo.roles/user)

(defroutes all-user-routes
  ;;TODO Just loging in the user it is not Updating the User 
  (PUT "/" [guid :as {params :params}] (user-api/update-user guid params))
  (GET "/" [guid] (-> guid user-api/find-user ))

  ;;Suggestion Sets
  (GET "/suggestion_sets" [guid :as {params :params}] (ss-api/find-suggestion-sets guid params))

  ;;Contacts
  (GET "/contacts" [guid & params] (c-api/list-contacts guid params))
  (GET "/contacts/:c-guid" [guid c-guid] (c-api/find-contact guid c-guid))
  (PATCH "/contacts/:c-guid" [guid c-guid & params] (c-api/update-contact guid c-guid params))
  
  ;;Messages
  (POST "/messages" [guid :as {params :params}] (m-api/send-message guid params))
;  (POST "/contacts/:c-guid/messages" [guid c-guid & params] (m-api/send-message guid c-guid params))

  ;;Stats
  (GET "/contact_stats" [guid] (s-api/get-contact-stats guid))
  (GET "/interaction_stats" [guid] (s-api/get-interaction-stats guid))


  ;;Threads
  (GET "/ui/:ui-guid/threads/:m-guid" [guid ui-guid m-guid] (t-api/load-thread guid ui-guid m-guid))
  (PUT "/ui/:ui-guid/threads/:m-guid" [guid ui-guid m-guid & params] (t-api/update-thread guid ui-guid m-guid params))
  (PATCH "/ui/:ui-guid/threads/:m-guid" [guid ui-guid m-guid & params] (t-api/update-thread guid ui-guid m-guid params)))

(defroutes APP-ROUTES
  (route/resources "/")

  (GET "/users" {params :params} (friend/authorize #{:zolo.roles/owner} (user-api/find-users params)))
  
  (context "/users/:guid" request (friend/authorize #{:zolo.roles/owner} all-user-routes))

  ;;anonymous access
  (POST "/users" {params :params} (-> params user-api/new-user))

  ;;Context IO
  (GET "/context_io/account" {params :params} (cio-api/get-account params))
  
  ;;GENERAL
  (GET "/server/status" {params :params} (server-api/status params))

  (route/not-found "Page not found"))

(def app
  (metrics-json/expose-metrics-as-json 
   (metrics-inst/instrument
    (zweb/wrap-request-binding
     (web/wrap-options
      (demonic/wrap-demarcation
       (params-mw/wrap-params
        (nested-params-mw/wrap-nested-params
         (wrap-json-params
          (kw-params-mw/wrap-keyword-params
           (web/wrap-user-info-logging
            (zweb/wrap-request-logging web/not-ignore-logging? web/logging-context identity #(assoc % :body "FILTERED")
                                       (zweb/wrap-error-handling
                                        (zweb/wrap-jsonify
                                         (web/wrap-accept-header-validation
                                          (friend/authenticate APP-ROUTES {:allow-anon? true
                                                                           :workflows [zauth/authenticate]
                                                                           :unauthenticated-handler #'zauth/return-forbidden
                                                                           :unauthorized-handler #'zauth/return-forbidden})))))))))))))) "/server/metrics" ))

(defn start-api
  ([]
     (start-api 4000))
  ([port]
     (zolo.setup.datomic-setup/init-datomic)
     (metrics-collector/start)
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
