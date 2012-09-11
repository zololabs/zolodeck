(ns zolo.core
  (:gen-class)
  (:use zolodeck.utils.debug
        compojure.core
        ring.adapter.jetty
        ring.middleware.params
        ring.middleware.keyword-params
        ring.middleware.json-params
        ring.middleware.nested-params
        [sandbar.auth]
        [sandbar.validation]
        [sandbar.stateful-session :only [wrap-stateful-session]]
        zolo.web)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [zolodeck.demonic.core :as demonic]
            [zolo.web.auth :as auth]
            [zolo.api.user-api :as user-api]))

(def security-policy
  [#"/permission-denied*" :any
   #"/users" :any
   #"/users.*" :user
   #".*" :user])

(defroutes application-routes
  (route/resources "/")

  ;;---- USER
  (POST "/users" [& params] (json-response (user-api/signup-user params)))
  (GET "/users/:id" [id] (json-response (current-user)))
 
  ;;---- FRIENDS
  (GET "/friends" [& params] (json-response (user-api/friends-list params)))

  ;;---- Contact Strength
  (GET "/contact-strengths" [& params] (json-response (user-api/contact-strengths params)))

  ;;---- User Stats
  (GET "/user-stats" [& params] (json-response (user-api/stats params)))
  
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
      { :headers    {"Access-Control-Allow-Origin"  "*"
                     "Access-Control-Allow-Methods"  "POST,PUT,OPTIONS"
                     "Access-Control-Allow-Headers"  "access-control-allow-origin,authorization,Content-Type"
                     "Access-Control-Allow-Credentials" "false"
                     "Access-Control-Max-Age" "60"}}
      (handler request))))

(def app
     (wrap-options
      (-> (handler/api application-routes)
          wrap-json-params
          (with-security security-policy auth/authenticator)
          wrap-stateful-session
          wrap-accept-header-validation
          wrap-error-handling
          demonic/wrap-demarcation
          wrap-request-logging
          )))

(defn -main []
  (run-jetty (var app) {:port 4000
                        :join? false}))



