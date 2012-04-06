(ns zolo.web
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.utils.debug
        clojure.stacktrace)
  (:require [clojure.data.json :as json]
            [zolo.web.status-codes :as http-status]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/json-str (print-vals "Data" data))})

(defn error-response [error-object]
  (json-response {:error (:message error-object)} ((:type error-object) http-status/codes)))

(defn wrap-error-handling [handler]
  (fn [request]
    (print-vals "wrap-error-handling")
    (try+
     (handler request)
     (catch [:type :bad-request] e
       (error-response e))
     (catch [:type :not-found] e
       (error-response e))
     (catch Exception e
       (print-stack-trace e)
       (json-response {:error (.getMessage e)} 500)))))

(defn valid-version? [accept-header-value]
  (= "application/vnd.zololabs.zolodeck.v1+json" accept-header-value))

(defn run-accept-header-validation [{:keys [headers]}]
  (if-not (valid-version? (headers "accept"))
    (throw+ {:type :bad-request
             :message "Invalid API version requested"})))

(defn wrap-accept-header-validation [handler]
  (fn [request]
    (print-vals "wrap-accept-header-validation")
    (run-accept-header-validation request)
    (handler request)))


