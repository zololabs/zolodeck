(ns zolo.test.core-utils
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.utils.debug)
  (:require [zolodeck.utils.clojure :as clojure]
            [zolo.setup.config :as conf])
  (:import [java.sql Time Date Timestamp]))

(def ^:dynamic *file-path-prefix* nil)

(zolo.setup.config/setup-config)
(zolo.setup.datomic-setup/init-datomic)

(defmacro is-not [body]
  `(is (not ~body)))

(defn timestamp-for-test [time-in-millis]
  (str (.toString (Date. time-in-millis)) " " (.toString (Time. time-in-millis))))

(defn now-for-test []
  (timestamp-for-test (System/currentTimeMillis)))

(defn run-all-zolo-tests 
  ([dir]
     (binding [*file-path-prefix* dir
               conf/ENV :test]
       (let [test-pkgs (clojure/find-ns-in-dir dir)]
         (doseq [pkg test-pkgs]
           (print-vals "Loading " pkg)
           (require pkg))
         (apply run-tests test-pkgs))))
  ([]
     (run-all-zolo-tests "test")))

(defn signed-request-for [fb-user-map]
  {:user_id (:id fb-user-map)})