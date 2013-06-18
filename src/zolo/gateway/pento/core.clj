(ns zolo.gateway.pento.core
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [zolo.utils.string :as zstring]
            [zolo.utils.maps :as zmaps]))

(defn classify-url [email-address name sent-count received-count]
  (->> {:email email-address :sent_count sent-count :received_count received-count :name name}
       zmaps/remove-nil-vals
       zstring/make-query-string
       (str "http://" (conf/pento-host) "/classify?")
       print-vals))

(defn pento-score [email-address name sent-count received-count]
  (let [json-map (-> (classify-url email-address name sent-count received-count)
                     http/get
                     :body
                     print-vals
                     json/read-json)]
    (-> email-address keyword json-map)))

(defn score [email-address name sent-count received-count]
  (float
   (let [s (try (pento-score email-address name sent-count received-count) (catch Exception e))]
     (or s 0.0))))