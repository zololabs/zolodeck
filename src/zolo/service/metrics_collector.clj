(ns zolo.service.metrics-collector
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.thread :as zthread]
            [zolo.metrics-ring.expose :as metrics]
            [clj-librato.metrics :as librato]
            [zolo.setup.config :as conf]
            [zolo.utils.logger :as logger]))

(defmulti harvest-value (fn [metrics-name metrics-map] (:type metrics-map)))

(defmethod harvest-value :meter [metrics-name metrics-map]
  {:name metrics-name :value (get-in metrics-map [:rates 1])})

(defmethod harvest-value :timer [metrics-name metrics-map]
  {:name metrics-name :value (get-in metrics-map [:rates 1])})

(defmethod harvest-value :counter [metrics-name metrics-map]
  {:name metrics-name :value (:value metrics-map)})

(defn collect-for-reporting [collector [metrics-name metrics-map]]
  (conj collector (harvest-value metrics-name metrics-map)))

(defn report-all-metrics []
  (logger/info "Gathering metrics to send out...")
  (it-> (metrics/all-metrics-clj)
        (reduce collect-for-reporting [] it)
        (librato/collate (conf/librato-username) (conf/librato-key) it []))
  (logger/info "... sent metrics!"))

(defn start []
  (zthread/run-thunk-periodically "LibratoReporter" report-all-metrics (* 30 1000)))