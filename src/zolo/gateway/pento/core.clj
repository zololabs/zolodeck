(ns zolo.gateway.pento.core
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.setup.config :as conf]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [zolo.utils.string :as zstring]
            [zolo.utils.maps :as zmaps]))

(def PENTO-BATCH-SIZE 500)

(defn pento-url []
  (str "http://" (conf/pento-host) "/classify"))

(defn- request-payload [email-info-list]
  {:body (-> email-info-list json/json-str (str "\n"))})

(defn scores [email-info-list]
  (time
   (it-> email-info-list
         (request-payload it)
         (http/post (pento-url) it)
         (:body it)
         (json/read-json it false))))

(defn score-all [all-email-info-list]
  (it-> all-email-info-list
        (partition-all PENTO-BATCH-SIZE it)
        (mapcat scores it)
        (apply merge it)
        (zmaps/transform-vals-with it #(float %2))))