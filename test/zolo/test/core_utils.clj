(ns zolo.test.core-utils
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        conjure.core)
  (:require [zolo.utils.clojure :as clojure]
            [zolo.setup.config :as conf]
            [zolo.setup.datomic-setup :as datomic-setup]
            [zolo.utils.calendar :as zolo-cal]
            [clj-time.core :as time])
  (:import [java.sql Time Date Timestamp]
           org.joda.time.DateTime))

(def ^:dynamic *file-path-prefix* nil)

(def ^:dynamic *generative-verbosity*)

(conf/setup-config)
(datomic-setup/init-datomic)

(defmacro is-not [body]
  `(is (not ~body)))

(defn timestamp-for-test [time-in-millis]
  (str (.toString (Date. time-in-millis)) " " (.toString (Time. time-in-millis))))

(defn now-for-test []
  (timestamp-for-test (System/currentTimeMillis)))

(defmacro run-as-of [yyyy-MM-dd-str & body]
  `(stubbing [zolo-cal/now-instant (zolo-cal/date-string->instant  (str ~yyyy-MM-dd-str " 23:59"))
              time/now (-> (zolo-cal/date-string->instant (str ~yyyy-MM-dd-str " 23:59"))
                           .getTime
                           DateTime.)
              zolo-cal/now (.getTime (zolo-cal/date-string->instant (str ~yyyy-MM-dd-str " 23:59")))]
     ~@body))

(defmacro demonic-integration-testing [doc & body]
  `(testing ~doc
     (zolo.setup.datomic-setup/reset)
     (try
       ~@body
       (finally
        (zolo.setup.datomic-setup/reset)))))

(defn run-all-zolo-tests 
  ([dir gen-verbosity]
     (time
      (binding [*file-path-prefix* dir
                *generative-verbosity* gen-verbosity
                conf/ENV :test]
        (let [test-pkgs (clojure/find-ns-in-dir dir)]
          (doseq [pkg test-pkgs]
            (print-vals "Loading " pkg)
            (require pkg))
          (apply run-tests test-pkgs)))))
  ([gen-verbosity]
     (run-all-zolo-tests "test" gen-verbosity))
  ([]
     (run-all-zolo-tests "test" false)))

(defn signed-request-for [fb-user-map]
  {:user_id (:id fb-user-map)})