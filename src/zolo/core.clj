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
        [sandbar.stateful-session :only [wrap-stateful-session]]
        zolo.web)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [zolodeck.demonic.core :as demonic]
            [zolo.web.auth :as auth]
            [zolo.social.bootstrap]            
            [zolo.social.core :as social]
            [zolo.api.user-api :as user-api]))

(def security-policy
  [#"/permission-denied*" :any
   #"/users" :any
   #"/users.*" :user
   #".*" :user])

(defroutes application-routes
  (route/resources "/")

  ;;---- USER
  (POST "/users" {params :params cookies :cookies} (json-response (user-api/signin-user params cookies)))  
  (GET "/users/:id" [id] (json-response (current-user)))
 
  ;;---- User Stats
  (GET "/user-stats" {params :params} (json-response (user-api/stats params)))
  
  ;;---- GENERAL
  (GET "/permission-denied*" []  (json-response {:error "Permission Denied"} 403))

  (route/not-found "Page not found"))

(defn wrap-request-logging [handler]
  (fn [request]
    (print-vals "REQUEST : " request)
    (let [response (handler request)]
      (print-vals "RESPONSE : " response)
      response)))

(defn wrap-options [handler]
  (fn [request]
    (if (= :options (request :request-method))
      { :headers {"Access-Control-Allow-Origin" (request-origin)
                  "Access-Control-Allow-Methods" "GET,POST,PUT,OPTIONS,DELETE"
                  "Access-Control-Allow-Headers" "access-control-allow-origin,authorization,Content-Type,origin,X-requested-with,accept"
                  "Access-Control-Allow-Credentials" "true"
                  "Access-Control-Max-Age" "60"}}
      (handler request))))

(def app
  (wrap-request-binding  
   (wrap-options
    (-> application-routes
        handler/api          
        wrap-json-params
        (with-security security-policy auth/authenticator)
        wrap-cookies        
        wrap-stateful-session
        wrap-accept-header-validation
        wrap-error-handling
        demonic/wrap-demarcation
        wrap-request-logging
        ))))

(defn -main []
  (zolo.setup.datomic-setup/init-datomic)
  (run-jetty (var app) {:port 4000
                        :join? false}))

