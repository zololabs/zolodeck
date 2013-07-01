(ns zolo.gateway.pento.core
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.setup.config :as conf]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [zolo.utils.string :as zstring]
            [zolo.utils.maps :as zmaps]))

(defn pento-url []
  (str "http://" (conf/pento-host) "/classify"))

;; (defn classify-url [email-address name sent-count received-count]
;;   (->> {:email email-address :sent_count sent-count :received_count received-count :name name}
;;        zmaps/remove-nil-vals
;;        zstring/make-query-string
;;        (str (pento-url) "?")
;;        print-vals))

;; (defn pento-score [email-address name sent-count received-count]
;;   (let [json-map (-> (classify-url email-address name sent-count received-count)
;;                      http/get
;;                      :body
;;                      print-vals
;;                      json/read-json)]
;;     (-> email-address keyword json-map)))

;; (defn score [email-address name sent-count received-count]
;;   (float
;;    (let [s (try (pento-score email-address name sent-count received-count) (catch Exception e))]
;;      (or s 0.0))))

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
        (partition-all 500 it)
        (mapcat scores it)
        (apply merge it)
        (zmaps/transform-vals-with it #(float %2))))