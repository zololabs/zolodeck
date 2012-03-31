(defproject com.currylogic/zolodeck-fe "0.1.0-SNAPSHOT"
  :description "Zolodeck FrontEnd"

  :dependencies [[org.clojure/clojure "1.4.0-beta3"]
                 [compojure "1.0.1"]
                 [ring "1.0.1"]
                 [slingshot "0.10.2"]]

  :plugins [[lein-swank "1.4.4"]
            [lein-pprint "1.1.0"]
            [lein-ring "0.6.2"]]
  
  :dev-dependencies [[clj-stacktrace "0.2.4"]
                     [swank-clojure "1.3.3"]]
  
  :min-lein-version "1.7.0"
  :test-selectors {:default (fn [t] (not (:integration t)))
                   :integration :integration
                   :all (fn [t] true)}
  
  :project-init (require 'clojure.pprint)
  :warn-on-reflection true
  
  :repositories {"jboss" "http://repository.jboss.org/nexus/content/groups/public/"
                 "local" ~(str (.toURI (java.io.File. "../mvn_repo")))}

  :ring {:handler fe.core/app}
  :main fe.core)