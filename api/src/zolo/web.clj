(ns zolo.web
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolodeck.utils.debug
        clojure.stacktrace)
  (:require [clojure.data.json :as json]
            [zolo.web.status-codes :as http-status]))

;; (defn- write-json-date [x out escape-unicode?]
;;   (.print out (date- x)))

(defn- write-json-uuid [x out escape-unicode?]
  (.print out (str x)))

(extend java.util.UUID json/Write-JSON
        {:write-json write-json-uuid})

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"
             "Access-Control-Allow-Origin", "*"}
   :body (json/json-str data)})

(defn error-response [error-object]
  (json-response {:error (:message error-object)} ((:type error-object) http-status/STATUS-CODES)))

(defn wrap-error-handling [handler]
  (fn [request]
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
    (run-accept-header-validation request)
    (handler request)))


