(ns fe.core
  (:gen-class)
  (:use compojure.core
        hiccup.core
        ring.adapter.jetty
        ring.middleware.params
        ring.middleware.keyword-params
        ring.middleware.nested-params)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defroutes application-routes
  (route/resources "/")
  
  (route/not-found "Page not found"))

(def app
     (-> (handler/site application-routes)))

(defn -main []
  (run-jetty (var app) {:port 8080
                        :join? false}))

