;; (ns leiningen.zolojars
;;   (:use robert.hooke
;;         leiningen.clean
;;         clojure.pprint
;;         [leiningen.core :only (apply-task read-project task-not-found)])
;;   (:require [clojure.java.shell :as shell])
;;   (:import [java.io File]))

;; (defn create-project [^File dir]
;;   (read-project (.getAbsolutePath (File. dir "project.clj"))))

;; (defn run-lein-task [task-name project]
;;   (println "Performing " task-name "on " (:name project))
;;   (apply-task task-name project [] task-not-found)
;;   (println "Successfully " task-name " - " (:name project)))

;; (defn exclude [p s]
;;   (assoc p :exclusions (conj (:exclusions p) (symbol s))))

;; (defn zolojars [project & args]
;;   (let [zolo-storm-project (-> (create-project (:dir project))
;;                                (exclude "org.slf4j/jcl-over-slf4j")
;;                                (assoc :name "zolodeck-api-storm"))]
;;     (run-lein-task "uberjar" project)
;;     (run-lein-task "uberjar" zolo-storm-project)
;;     (println "\n")
;;     (println "*************************************************************")
;;     (println "Need to run lein deps again before you start development work")
;;     (println "*************************************************************")))


