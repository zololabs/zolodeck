(defproject com.currylogic/zolodeck-api "0.1.0-SNAPSHOT"
  :description "Zolodeck API"

  :dependencies [[org.clojure/clojure "1.4.0"]

                 [compojure "1.0.2" ]
                 [ring "1.0.2"]

                 [fuziontech/ring-json-params "0.2.0"]
                 
                 [sandbar/sandbar "0.4.0-SNAPSHOT"]

                 [org.clojure/data.json "0.1.2"]
                 [clj-http "0.5.3" :exclusions [commons-logging]]

                 [joda-time "1.6"]
                 [clj-time "0.4.4"]
                 [slingshot "0.10.2"]

                 ;; [org.clojars.nakkaya.javax.mail/imap "1.4.3"]
                 ;; [org.clojars.nakkaya.javax.mail/mail "1.4.3"]

                 [org.clojars.amit/zolo_fb_chat "0.0.1"]
                 
                 [org.clojure/core.match "0.2.0-alpha9"]

                 [org.scribe/scribe "1.3.2"] ;;oauth for LinkedIn

                 ;;Logging Related Stuff
                 [org.clojure/tools.logging "0.2.4"]
                 [ch.qos.logback/logback-classic "1.0.7"]
                 [ch.qos.logback/logback-core "1.0.6"]
                 [org.slf4j/slf4j-api "1.7.0"]
                 [clj-logging-config "1.9.10" :exclusions [log4j]]
                 [me.moocar/logback-gelf "0.9.6p2"]

                 [org.clojure/tools.cli "0.2.2"]

                 [com.datomic/datomic "0.8.3704"]
                 [com.netflix.curator/curator-framework "1.0.1"]
                 [zolodeck/demonic "0.1.0-SNAPSHOT" :exclusions [com.datomic/datomic-free]]

                 [zolodeck/zolo-utils "0.1.0-SNAPSHOT"]
                 [world-country-list "1.0.0-SNAPSHOT"]
                 
                 [org.clojure/tools.cli "0.2.2"]
                 ]

  ;; Global exclusions are applied across the board, as an alternative
  ;; to duplication for multiple depedencies with the same excluded libraries.
  :exclusions [org.clojure/clojure
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-api
               org.slf4j/slf4j-nop
               org.slf4j/log4j-over-sl4f
               org.slf4j/jul-to-slf4j
               log4j/log4j
               log4j
               clj-time
               org.netflix.curator/curator-framework
               commons-logging/commons-logging
               org.clojure/tools.logging]

  :plugins [[lein-swank "1.4.4"]
            [lein-pprint "1.1.1"]
            [lein-ring "0.6.2"]
            [lein-difftest "1.3.8"]
            [lein-notes "0.0.1"]
            [lein-deploy-app "0.1.0"]]

  :hooks [leiningen.hooks.difftest]

  :dev-dependencies [[storm "0.8.2-wip20" :exclusions [org.slf4j/log4j-over-sl4fj
                                                       org.slf4j/slf4j-log4j12
                                                       com.netflix.curator/curator-framework]]
                     [clj-stacktrace "0.2.4"]
                     [swank-clojure "1.3.3"]
                     [ring-serve "0.1.2"]
                     [zolodeck/clj-social-lab "1.0.0-SNAPSHOT"]
                     [difform "1.1.2"]]
  
  :min-lein-version "1.7.0"

  :test-selectors {:default (fn [t] (not (:integration t)))
                   :integration :integration
                   :all (fn [t] true)}
  
  :project-init (do (use 'ring.util.serve) 
                    (use 'clojure.pprint)
                    (use 'clojure.test)
                    (use 'com.georgejahad.difform)
                    (use 'zolo.utils.readers)
                    (use 'zolodeck.demonic.core)
                    (use 'zolo.test.core-utils))

  :warn-on-reflection false
  
  :repositories {"jboss" "http://repository.jboss.org/nexus/content/groups/public/"
                 "local_repo" ~(str (.toURI (java.io.File. "mvn_repo")))}
  
  :resources-path "config"

  :deploy-app {:s3-bucket "s3p://zolodeck/releases/"
               :creds :env}

  :main zolo.core

  :jvm-opts ["-Xmx1g"
             "-server"
             ;; "-agentpath:/Applications/YourKit_Java_Profiler_11.0.9.app/bin/mac/libyjpagent.jnilib"
             ]
  
  :repl-retry-limit 1000)