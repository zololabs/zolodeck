(ns zolo.web
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolo.utils.debug
        clojure.stacktrace)
  (:require [clojure.data.json :as json]
            [zolo.utils.http-status-codes :as http-status]
            [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolo.utils.web :as zweb]
            [zolo.setup.config :as config]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.utils.maps :as zmaps]
            [zolo.utils.string :as zolo-str]))

(def RANDOM-PROCESS-ID (java.util.UUID/randomUUID))

(def PROCESS-COUNTER (atom 0))

(defn- write-json-uuid [x out escape-unicode?]
  (.print out (str "\"" x "\"")))

(defn- format-joda-time [d]
  (str (clj-time.format/unparse (clj-time.format/formatters :date) d) " "
       (clj-time.format/unparse (clj-time.format/formatters :hour-minute) d)))

(extend java.util.UUID json/Write-JSON
        {:write-json write-json-uuid})

(extend java.util.Date json/Write-JSON
        {:write-json (fn [d out escape-unicode?]
                       (.print out (str "\"" (zolo-cal/date-to-string d (zolo-cal/simple-date-format "yyyy-MM-dd hh:mm")) "\"")))})

(extend org.joda.time.DateTime json/Write-JSON
        {:write-json (fn [d out escape-unicode?]
                       (.print out (str "\"" (format-joda-time d) "\"")))})

;; (extend zolo.demonic.loadable.Loadable json/Write-JSON
;;         {:write-json (fn [x out escape-unicode?] (.print out
;;         (json/json-str(.m x))))})

(defn valid-version? [uri accept-header-value]
  (or (= "/server/status" uri)
      (= "application/vnd.zololabs.zolodeck.v1+json" accept-header-value)))

(defn run-accept-header-validation [{:keys [uri headers] :as req}]
  (if-not (valid-version? uri (headers "accept"))
    (throw+ {:type :bad-request
             :message "Invalid API version requested"})))

(defn wrap-accept-header-validation [handler]
  (fn [request]
    (run-accept-header-validation request)
    (handler request)))

(defn wrap-options [handler]
  (fn [request]
    (if (= :options (request :request-method))
      { :headers {"Access-Control-Allow-Origin" (zweb/request-origin)
                  "Access-Control-Allow-Methods" "GET,POST,PUT,OPTIONS,DELETE"
                  "Access-Control-Allow-Headers" "access-control-allow-origin,authorization,Content-Type,origin,X-requested-with,accept"
                  "Access-Control-Allow-Credentials" "true"
                  "Access-Control-Max-Age" "60"}}
      (handler request))))

(defn not-ignore-logging? [request]
  (nil? (#{"/server/status"} (:uri request))))

(defn- user-guid-from-request [request]
  (or (-> request :params :guid)
      (let [splitted-uri (->> request :uri (zolo-str/split "/"))]    
        (when (and (>= (count splitted-uri) 3) (= (second splitted-uri) "users"))
          (nth splitted-uri 2)))))

(defn wrap-user-info-logging [handler]
  (fn [request]
    (if (not-ignore-logging? request)
      (do
        (logger/with-logging-context {:guid (user-guid-from-request request)}
          (handler request)))
      (handler request))))

(defn guid-from-cookie [request]
  (get-in request [:cookies "zolo_guid" :value]))

(defn trace-id [request]
  (str ".env-" (config/environment)
       ".h-" (:host request)
       ".rh-" RANDOM-PROCESS-ID
       ".c-" @PROCESS-COUNTER
       ".ts-" (zolo-cal/now)  
       ".v-" (config/git-head-sha)))

(defn logging-context [request]
  (swap! PROCESS-COUNTER inc)
  (merge
   (select-keys request [:request-method :query-string :uri :server-name])
   {:trace-id (trace-id request)
    :env (config/environment)
    :ip-address (get-in request [:headers "x-real-ip"])}))

