(ns zolo.test.web-utils
  (:use zolodeck.utils.maps
        [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.utils.debug)
  (:require [zolo.core :as server]
            [clojure.data.json :as json]))

(defn compojure-request [method resource jsonified-body-str params]
  {:request-method method 
   :uri resource
   :params params
   :body (java.io.StringReader. jsonified-body-str)
   :headers {"accept" "application/vnd.zololabs.zolodeck.v1+json"
             "content-type" "application/json; charset=UTF-8"}
   :content-type "application/json; charset=UTF-8"})

(defn web-request
  ([method resource body]
     (web-request method resource body {}))
  ([method resource body params]
     (-> (compojure-request method resource (json/json-str body) params)
         server/app
         (update-in [:body] json/read-json))))

;; (defn was-response-status? [{:keys [web-response] :as scenario} expected-status]
;;   (let [{:keys [status headers body]} web-response]
;;     (is (= expected-status status)))
;;   scenario)

;; (defn was-request-successful? [scenario]
;;   (was-response-status? scenario 200)
;;   scenario)

;; (defn new-user-url []
;;   "/users")