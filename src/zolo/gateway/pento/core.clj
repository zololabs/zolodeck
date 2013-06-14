(ns zolo.gateway.pento.core
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [clj-http.client :as http]
            [clojure.data.json :as json]))

(defn classify-url [email-address]
  (print-vals (str "http://" (conf/pento-host) "/classify?email=" email-address)))

(defn pento-score [email-address]
  (let [json-map (-> email-address
                     classify-url
                     http/get
                     :body
                     print-vals
                     json/read-json)]
    (-> email-address keyword json-map)))

(defn score [email-address]
  (float
   (let [s (try (pento-score email-address) (catch Exception e))]
     (or s 0.0))))