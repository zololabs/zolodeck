(defproject zololabs/zolodeck-api "0.1.0-SNAPSHOT"
  :description "Zolodeck API"

  :dependencies [[org.clojure/clojure "1.5.1"]

                 [compojure "1.1.5" ]
                 [ring/ring-core "1.2.0-beta1"]

                 [fuziontech/ring-json-params "0.2.0"]
                 
                 [com.cemerick/friend "0.1.5"]

                 [org.clojure/data.json "0.1.2"]
                 [clj-http "0.5.3" :exclusions [commons-logging]]

                 [clj-time "0.4.4"]
                 [slingshot "0.10.3"]

                 [org.clojars.amit/zolo_fb_chat "0.0.1"]
                 
                 [org.clojure/core.match "0.2.0-alpha11"]

                 [org.scribe/scribe "1.3.2"] ;;oauth for LinkedIn
                 [zololabs/context-io-clj "e114172"]
                 
                 ;;Logging Related Stuff
                 [org.clojure/tools.logging "0.2.4"]
                 [ch.qos.logback/logback-classic "1.0.7"]
                 [ch.qos.logback/logback-core "1.0.6"]
                 [ch.qos.logback.contrib/logback-json-classic "0.1.2"]
                 [ch.qos.logback.contrib/logback-jackson "0.1.2"]
                 [org.codehaus.jackson/jackson-core-asl "1.9.12"]
                 [com.fasterxml.jackson.core/jackson-databind "2.2.2"]
                 [org.slf4j/slf4j-api "1.7.0"]
                 [clj-logging-config "1.9.10" :exclusions [log4j]]

                 [org.clojure/tools.cli "0.2.2"]

                 [com.datomic/datomic "0.8.4007"]
                 [com.netflix.curator/curator-framework "1.0.1"]
                 [zololabs/demonic "0.1.0-SNAPSHOT" :exclusions [com.datomic/datomic-free]]

                 [zololabs/zolo-utils "0.1.0-SNAPSHOT"]
                 [zololabs/world-country-list "1.1.0"]
                 
                 [org.clojure/tools.cli "0.2.2"]
                 ]

  ;; Global exclusions are applied across the board, as an alternative
  ;; to duplication for multiple depedencies with the same excluded libraries.
  :exclusions [org.clojure/clojure
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-api
               org.slf4j/slf4j-nop
               ;org.slf4j/log4j-over-slf4j
               ;org.slf4j/jcl-over-slf4j
               ;org.slf4j/jul-to-slf4j
               log4j/log4j
               log4j
               clj-time
               org.netflix.curator/curator-framework
               commons-logging/commons-logging
               org.clojure/tools.logging
               org.clojure/clojure-contrib]

  :plugins [[lein-pprint "1.1.1"]
            [lein-ring "0.6.2"]
            [lein-cloverage "1.0.2"]]

  :resource-paths [~(str (System/getProperty "user.home") "/.zolo")]

  :profiles {:dev {:dependencies [[clj-stacktrace "0.2.4"]
                                  [ring-serve "0.1.2"]
                                  [zololabs/marconi "1.0.0-SNAPSHOT"]
                                  [org.clojars.runa/conjure "2.1.1"]
                                  [difform "1.1.2"]
                                  [org.clojure/data.generators "0.1.0"]
                                  [org.clojure/math.combinatorics "0.0.4"]
                                  [storm/storm-lib "0.8.2"]]}
             :1.4 [:dev
                   {:dependencies [[org.clojure/clojure "1.4.0"]]}]
             :storm-local [:dev
                           :1.4
                           {:main zolo.storm.core}]
             :storm [:dev
                     :1.4
                     {:resource-paths ^:replace [~(str "devops/config/prod")]
                      :jar-exclusions [#"log4j\.properties" #"backtype" #"trident"
                                       #"META-INF" #"meta-inf" #"\.yaml" #"org\/slf4j\/"]
                      :uberjar-exclusions [#"log4j\.properties" #"backtype" #"trident"
                                           #"META-INF" #"meta-inf" #"\.yaml" #"org\/slf4j\/"]
                      :aot :all
                      :main zolo.storm.facebook
                      :uberjar-name ~(str "zolodeck-storm-"
                                          (or (System/getenv "BUILD_NUMBER") "local")
                                          "-"
                                          (or (System/getenv "BUILD_ID") "")
                                          "-standalone.jar")}]}
  
  :uberjar-name ~(str "zolodeck-api-"
                      (or (System/getenv "BUILD_NUMBER") "local")
                      "-"
                      (or (System/getenv "BUILD_ID") "")
                      "-standalone.jar")
  
  :min-lein-version "2.0.0"

  :test-selectors {:default (fn [t] (not (:integration t)))
                   :integration :integration
                   :storm :storm
                   :all (fn [t] true)}

  :repl-options {:init-ns zolo.core
                 :init (do (use 'zolo.core)
                           (use 'ring.util.serve) 
                           (use 'clojure.pprint)
                           (use 'clojure.test)
                           (use 'com.georgejahad.difform)                           
                           (use 'zolo.utils.readers)
                           (use 'zolo.demonic.core)
                           (use 'zolo.test.core-utils)
                           (use 'zolo.setup.config))}

  :warn-on-reflection false
  
  :repositories {"jboss" "http://repository.jboss.org/nexus/content/groups/public/"
                 "local_repo" ~(str (.toURI (java.io.File. "mvn_repo")))}
  
  :bootclasspath false

  :deploy-app {:s3-bucket "s3p://zolodeck/releases/"
               :creds :env}

  :main zolo.core

  :jvm-opts ["-Xmx1g" "-server"
             ;"-agentpath:/Applications/YourKit_Java_Profiler_11.0.9.app/bin/mac/libyjpagent.jnilib"
             ])