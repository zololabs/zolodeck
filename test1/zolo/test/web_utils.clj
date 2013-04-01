(ns zolo.test.web-utils
  (:use zolo.utils.maps
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.core :as server]))

(defn compojure-request [method resource stringified-params]
  {:request-method method 
   :uri resource 
   :params stringified-params
   :headers {"accept" "application/vnd.zololabs.zolodeck.v1+json"
             "authorization" "fb DUMMYONE"}})

(defn web-request [method resource params]
  (server/app (compojure-request method resource (stringify-map params))))

(defn was-response-status? [{:keys [web-response] :as scenario} expected-status]
  (let [{:keys [status headers body]} web-response]
    (is (= expected-status status)))
  scenario)

(defn was-request-successful? [scenario]
  (was-response-status? scenario 200)
  scenario)

(defn new-user-url []
  "/users")

(defn friends-list-url []
  "/friends")