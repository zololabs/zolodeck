(ns zolo.gateway.pento.core
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.setup.config :as conf]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [zolo.utils.string :as zstring]
            [zolo.utils.maps :as zmaps]))

(def PENTO-BATCH-SIZE 1000)

(defn pento-url []
  (str "https://" (conf/pento-host) "/classify"))

(defn- request-payload [email-info-list]
  {:body (-> {:emails email-info-list}
             json/json-str)
   :content-type "application/json"})

(defn process-response [pento-response]
  (if pento-response
    (json/read-json pento-response false)
    (print-vals "Got empty response..." [])))

(defn scores [email-info-list]
  (print-vals "Scoring contact batch of size:" (count email-info-list))
  (time
   (->> email-info-list
        request-payload
        (http/post (pento-url))
        :body
        process-response)))

(defn score-all [all-email-info-list]
  (it-> all-email-info-list
        (partition-all PENTO-BATCH-SIZE it)
        (map scores it)
        (apply merge it)
        (zmaps/transform-vals-with it #(float %2))))