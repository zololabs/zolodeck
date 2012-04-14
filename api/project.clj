(defproject com.currylogic/zolodeck-api "0.1.0-SNAPSHOT"
  :description "Zolodeck API"

  :dependencies [[org.clojure/clojure "1.4.0-beta3"]

                 [compojure "1.0.2" :exclude [org.clojure/clojure]]
                 [ring "1.0.2" :exclude [org.clojure/clojure]]

                 [sandbar/sandbar "0.4.0-SNAPSHOT" :exclude [org.clojure/clojure]]

                 ;; clj-facebook-graph Related Deps
                 ;;[zololabs/zolo-clj-facebook-graph "0.4.0"]
                 [org.clojure/data.json "0.1.2"  :exclude [org.clojure/clojure]]
                 [clj-http "0.3.6"]
                 [clj-oauth2 "0.2.0"]

                 [joda-time "1.6"]
                 [clj-time "0.3.7"]
                 [slingshot "0.10.2"]

                 [org.clojars.nakkaya.javax.mail/imap "1.4.3"]
                 [org.clojars.nakkaya.javax.mail/mail "1.4.3"]

                 [org.clojure/core.match "0.2.0-alpha9"]

                 [com.datomic/datomic "0.1.2753"]]

  :plugins [[lein-swank "1.4.4"]
            [lein-pprint "1.1.1"]
            [lein-ring "0.6.2"]
            [lein-difftest "1.3.8"]
            [lein-notes "0.0.1"]]

  :hooks [leiningen.hooks.difftest]

  :dev-dependencies [[clj-stacktrace "0.2.4"]
                     [swank-clojure "1.3.3"]
                     [ring-serve "0.1.2"]]
  
  :min-lein-version "1.7.0"
  :test-selectors {:default (fn [t] (not (:integration t)))
                   :integration :integration
                   :all (fn [t] true)}
  
  :project-init (do (use 'ring.util.serve) 
                    (use 'clojure.pprint)
                    (use 'clojure.test))

  :warn-on-reflection false
  
  :repositories {"jboss" "http://repository.jboss.org/nexus/content/groups/public/"
                 "local" ~(str (.toURI (java.io.File. "../mvn_repo")))}
  
  :resources-path "config"

  :main zolo.core)