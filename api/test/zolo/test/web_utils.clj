(ns zolo.test.web-utils
  (:use zolo.utils.maps)
  (:require [zolo.core :as server]))

(defn compojure-request [method resource stringified-params]
  {:request-method method 
   :uri resource 
   :params stringified-params
   :headers {"accept" "application/vnd.zololabs.zolodeck.v1+json"
             "authorization" "fb DUMMYONE"}})

(defn web-request [method resource params]
  (server/app (compojure-request method resource (stringify-map params))))

(defn new-user-url []
  "/users")