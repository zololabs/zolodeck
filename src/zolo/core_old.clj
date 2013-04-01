;; (ns zolo.core-old
;;   (:gen-class)
;;   (:use zolo.utils.debug
;;         compojure.core
;;         ring.adapter.jetty
;;         ring.middleware.params
;;         ring.middleware.cookies
;;         ring.middleware.keyword-params
;;         ring.middleware.json-params
;;         ring.middleware.nested-params
;;         ring.middleware.cookies
;;         [sandbar.auth]
;;         [sandbar.validation]
;;         [sandbar.stateful-session :only [wrap-stateful-session]])
;;   (:require [compojure.route :as route]
;;             [compojure.handler :as handler]
;;             [clojure.tools.cli :as cli]
;;             [zolodeck.demonic.core :as demonic]
;;             [zolo.web.auth :as auth]
;;             [zolo.social.bootstrap]
;;             [zolo.social.core :as social]
;;             [zolo.api.user-api :as user-api]
;;             [zolo.api.contact-api :as contact-api]
;;             [zolo.api.server-api :as server-api]
;;             [zolo.utils.logger :as logger]
;;             [zolo.web :as web]
;;             [zolo.setup.config :as config]
;;             [zolo.storm.facebook :as fb]))

;; (def security-policy
;;   [#"/permission-denied*" :any
;;    #"/server/status" :any
;;    #"/users" :any
;;    #"/users/*" :user
;;    #"/contacts/*" :user
;;    #".*" :user])

;; (defroutes application-routes
;;   (route/resources "/")

;;   ;;---- USER
;;   ;; (POST "/users" {params :params cookies :cookies} (web/json-response (user-api/signin-user params cookies)))
;;   ;; (PUT "/users/:guid" {params :params cookies :cookies} (web/json-response (user-api/signin-user params cookies)))  
;;   ;; (GET "/users/:guid" [guid] (web/json-response (current-user)))

;;   ;; ;;--- Contact
;;   ;; (GET "/contacts" {params :params} (web/json-response (contact-api/list-contacts params)))
;;   ;; (PUT "/contacts/:guid" {params :params} (web/json-response (contact-api/update-contact params)))

;;   ;; (POST "/messages" {params :params} (web/json-response (user-api/send-message params)))
  
;;   ;; ;;---- User Stats
;;   ;; (GET "/user-stats" {params :params} (web/json-response (user-api/stats params)))

;;   ;; ;;---- Server Status
;;   ;; (GET "/server/status" {params :params} (web/json-response (server-api/status params)))
  
;;   ;;---- GENERAL
;;   (GET "/permission-denied*" []  (web/json-response {:error "Permission Denied"} 403))

;;   (route/not-found "Page not found"))

;; (def app
;;   (web/wrap-request-binding  
;;    (web/wrap-options
;;     (-> application-routes        
;;         web/wrap-user-info-logging
;;         handler/api
;;         wrap-json-params
;;         (with-security security-policy auth/authenticator)
;;         web/wrap-accept-header-validation
;;         wrap-stateful-session
;;         web/wrap-error-handling
;;         demonic/wrap-demarcation
;;         web/wrap-request-logging
;;         wrap-cookies        
;;         ))))


;; (defn start-api
;;   ([]
;;      (start-api 4000))
;;   ([port]
;;      (zolo.setup.datomic-setup/init-datomic)
;;      (run-jetty (var app) {:port port
;;                            :join? false})))  

;; (defn start-storm []
;;   (zolo.setup.datomic-setup/init-datomic)
;;   (logger/with-logging-context {:env (config/environment)}
;;     (fb/run-local-forever!)))

;; (defn process-args [args]
;;   (cli/cli args
;;            ["-s"  "--service" "storm/api" :default "api" :parse-fn #(keyword (.toLowerCase %))]
;;            ["-p" "--port" "Listen on this port" :default 4000  :parse-fn #(Integer. %)] 
;;            ["-h" "--help" "Show help" :default false :flag true]))

;; (defn -main [& cl-args]
;;   (config/setup-config)
;;   (print-vals "CL Args :" cl-args)
;;   (let [[options args banner] (process-args cl-args)]
;;     (when (:help options)
;;       (println banner)
;;       (System/exit 0))
;;     (condp = (:service (print-vals "Options :" options))
;;         :storm (start-storm)
;;         :api (start-api (:port options))
;;         :default (throw "Invalid Service :" (:s options)))))

