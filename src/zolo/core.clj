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
            [zolodeck.demonic.core :as demonic]
            [zolo.web.auth :as auth]
            [zolo.social.bootstrap]            
            [zolo.social.core :as social]
            [zolo.api.user-api :as user-api]
            [zolo.utils.logger :as logger]
            [zolo.web :as web]))

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

(defn -main []
  (zolo.setup.datomic-setup/init-datomic)
  (run-jetty (var app) {:port 4000
                        :join? false}))

