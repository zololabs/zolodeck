(ns zolo.core
  (:gen-class)
  (:use zolo.utils.debug
        compojure.core
        ring.adapter.jetty
        ring.middleware.params
        ring.middleware.keyword-params
        ring.middleware.nested-params
        [sandbar.auth]
        [sandbar.validation]
        [sandbar.stateful-session :only [wrap-stateful-session]]
        zolo.web)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
;;            [zolo.auth :as auth]
            [zolo.api.user-api :as user-api]))

(def security-policy
     [#"/permission-denied*" :any
      #".*" :user])

(defroutes application-routes
  (route/resources "/")

  ;;---- USER
  (POST "/users" [& params] (user-api/upsert-user params))
 
  ;;---- GENERAL
  (GET "/permission-denied*" [] {:status 403 :body "Permission Denied"})
  (route/not-found "Page not found"))

(defn wrap-request-logging [handler]
  (fn [request]
    (print-vals "wrap-request-logging")
    (print-vals "REQUEST : " request)
    (let [response (handler request)]
      (print-vals "RESPONSE : " response)
      response)))

(defn wrap-options [handler]
  (fn [request]
    (print-vals "wrap-options")
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
;;          (with-security security-policy auth/authenticator)
;;          wrap-stateful-session
          wrap-accept-header-validation
          wrap-error-handling
          wrap-request-logging)))

(defn -main []
  (run-jetty (var app) {:port 4000
                        :join? false}))

