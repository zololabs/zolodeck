(ns zolo.gateway.pento.core
  (:require [zolo.setup.config :as conf]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            ))

(defn classify-url [email-address]
  (str "http://" (conf/pento-host) "?email=" email-address))

(defn score [email-address]
  (-> email-address
      classify-url
      http/get
      json/read-json
      :score))
