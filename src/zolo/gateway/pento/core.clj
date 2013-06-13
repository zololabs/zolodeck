(ns zolo.gateway.pento.core
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [clj-http.client :as http]
            [clojure.data.json :as json]))

(defn classify-url [email-address]
  (print-vals (str "http://" (conf/pento-host) "/classify?email=" email-address)))

(defn score [email-address]
  (let [json-map (-> email-address
                     classify-url
                     http/get
                     :body
                     json/read-json)]
    (-> email-address keyword json-map float)))
