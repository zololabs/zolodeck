(ns leiningen.build-checkouts
  (:use robert.hooke
        leiningen.clean
        clojure.pprint
        [leiningen.core :only (apply-task read-project task-not-found)])
  (:require [clojure.java.shell :as shell])
  (:import [java.io File]))

(defn error [msg-format & params]
  (println (apply format (str "ERROR: " msg-format) params))
  (System/exit 1))

(defn user-dir []
  (-> "user.dir" System/getProperty File.))

(defn has-project-clj [^File dir]
  (not (nil? (read-project (.getAbsolutePath (File. dir "project.clj"))))))

(defn list-checkouts [^File dir]
  (filter has-project-clj (seq (.listFiles (File. dir "checkouts")))))

(defn create-project [^File dir]
  (read-project (.getAbsolutePath (File. dir "project.clj"))))

(defn run-lein-task [task-name project]
  (println "Performing " task-name "on " (:name project))
  (apply-task task-name project [] task-not-found)
  (println "Successfully " task-name " - " (:name project)))

(defn build-checkouts [project & args]
  (doseq [p (map create-project (list-checkouts (:dir project)))]
    (run-lein-task "compile" p)))


