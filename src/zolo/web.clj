(ns zolo.web
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolodeck.utils.debug
        clojure.stacktrace)
  (:require [clojure.data.json :as json]
            [zolo.web.status-codes :as http-status]
            [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolo.setup.config :as config]))

(def ^:dynamic *ZOLO-REQUEST*)

(defn request-origin []
  (get-in *ZOLO-REQUEST* [:headers "origin"]))

(defn- write-json-uuid [x out escape-unicode?]
  (.print out (str "'" x "'")))

(extend java.util.UUID json/Write-JSON
        {:write-json write-json-uuid})

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"
             "Access-Control-Allow-Origin" (request-origin)
             "Access-Control-Allow-Credentials" "true"}
   :body (json/json-str data)})

(defn error-response [error-object]
  (json-response {:error (:message error-object)} ((:type error-object) http-status/STATUS-CODES)))

(defn wrap-error-handling [handler]
  (fn [request]
    (try+
     (handler request)
     (catch [:type :bad-request] e
       (logger/error "Bad Request :" e)
       (error-response e))
     (catch [:type :not-found] e
       (logger/error "Not found :" e)
       (error-response e))
     (catch Exception e
       (logger/error "Exception Occured :" e)
       (json-response {:error (.getMessage e)} 500)))))

(defn valid-version? [accept-header-value]
  (or true (= "application/vnd.zololabs.zolodeck.v1+json" accept-header-value)))

(defn run-accept-header-validation [{:keys [headers]}]
  (if-not (valid-version? (headers "accept"))
    (throw+ {:type :bad-request
             :message "Invalid API version requested"})))

(defn wrap-accept-header-validation [handler]
  (fn [request]
    (run-accept-header-validation request)
    (handler request)))

(defn wrap-request-binding [handler]
  (fn [request]
    (binding [*ZOLO-REQUEST* request]
      (handler request))))

(defn wrap-options [handler]
  (fn [request]
    (if (= :options (request :request-method))
      { :headers {"Access-Control-Allow-Origin" (request-origin)
                  "Access-Control-Allow-Methods" "GET,POST,PUT,OPTIONS,DELETE"
                  "Access-Control-Allow-Headers" "access-control-allow-origin,authorization,Content-Type,origin,X-requested-with,accept"
                  "Access-Control-Allow-Credentials" "true"
                  "Access-Control-Max-Age" "60"}}
      (handler request))))

(defn wrap-user-info-logging [handler]
  (fn [request]
    (logger/debug "Current User loaded? : " (not (nil? (user/current-user))))
    (logger/with-logging-context {:guid (:user/guid (user/current-user))}
      (handler request))))

(defn guid-from-cookie [request]
  (get-in request [:cookies "zolo_guid" :value]))

(defn logging-context [request]
  (merge
   (select-keys request [:request-method :query-string :uri :server-name])
   ;;TODO Create trace-id 
   {:trace-id (str (rand 1000000))
    :environment (config/environment)
    :ip-address (get-in request [:headers "x-real-ip "])
    :guid (guid-from-cookie request)}))

(defn wrap-request-logging [handler]
  (fn [request]
    (logger/with-logging-context (logging-context request)
      (logger/debug "REQUEST : " request)
      (let [response (handler request)]
        (logger/debug "RESPONSE : " response)
        response))))
