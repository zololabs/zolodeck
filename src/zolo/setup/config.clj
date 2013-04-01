(ns zolo.setup.config
  (:use zolo.utils.clojure
        zolo.utils.debug)
  (:require [clojure.java.io :as java-io]
            [zolo.utils.logger :as logger]))

(declare CONFIG-MAP)
(declare ^:dynamic ENV)

(defn environment []
  (get-in CONFIG-MAP [:zolodeck-env]))

(defn load-config [config-file]
  (let [config-map (load-string (slurp config-file))]
    (def CONFIG-MAP config-map)
    (def ^:dynamic ENV (environment))))

(defn production-mode? []
  (= :production ENV))

(defn datomic-db-name [] 
  (get-in CONFIG-MAP [:configs ENV :datomic-db]))

(defn fb-app-id []
  (get-in CONFIG-MAP [:configs ENV :fb-app-id]))

(defn fb-app-secret []
  (get-in CONFIG-MAP [:configs ENV :fb-app-secret]))

(defn user-update-wait-fb-millis []
  (get-in CONFIG-MAP [:configs ENV :user-update-wait-fb-millis]))

(defn new-user-freshness-millis []
  (get-in CONFIG-MAP [:configs ENV :new-user-freshness-millis]))

(defn context-io-key []
  (get-in CONFIG-MAP [:configs ENV :context-io-key]))

(defn context-io-secret []
  (get-in CONFIG-MAP [:configs ENV :context-io-secret]))

(defn li-api-key []
  (get-in CONFIG-MAP [:configs ENV :li-api-key]))

(defn li-secret-key []
  (get-in CONFIG-MAP [:configs ENV :li-secret-key]))

(defn system-properties []
  (get-in CONFIG-MAP [:configs ENV :system-properties]))

(defn set-system-properties [properties]
  (map (fn [[k v]]
         (System/setProperty k v)) properties))

(defn get-env-var [v]
  (.get (System/getenv) v))

(defrunonce setup-config []
  (let [config-file (str (System/getProperty "user.home") "/.zolo/zolo.clj")]
    (logger/trace "Loading config file:" config-file)
    (load-config config-file)
    (set-system-properties (system-properties))))

(defn fb-auth-cookie-name []
  (str "fbsr_" (fb-app-id)))

(defn li-auth-cookie-name []
  (str "linkedin_oauth_" (li-api-key)))

(defn git-head-sha []
  (or (get-env-var "GIT_HEAD_SHA") "GIT-SHA-NOT-SET"))