(defproject com.currylogic/zolodeck-api "0.1.0-SNAPSHOT"
  :description "Zolodeck API"

  :dependencies [[org.clojure/clojure "1.4.0-beta3"]
                 [compojure "1.0.0-RC2"]
                 [ring "1.0.1"]
                 [sandbar/sandbar "0.4.0-SNAPSHOT"]
                 [clj-facebook-graph "0.4.0"]
                 [clj-base64 "0.0.1"]
                 [org.bituf/clj-stringtemplate "0.2"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [bouncycastle/bcprov-jdk16-nosign "140"]
                 [clj-http "0.1.3"]
                 [joda-time "1.6"]
                 [slingshot "0.10.2"]

                 [org.clojars.nakkaya.javax.mail/imap "1.4.3"]
                 [org.clojars.nakkaya.javax.mail/mail "1.4.3"]


                 [org.codehaus.janino/janino "2.5.16"]
                 [org.apache.lucene/lucene-core "3.3.0"]
                 [org.hornetq/hornetq-core "2.2.2.Final"]
                 [com.amazonaws/aws-java-sdk "1.3.0"]

                 [commons-io "1.4"]
                 [org.apache.commons/commons-exec "1.1"]
                 [storm/libthrift7 "0.7.0"]
                 [clj-time "0.3.0"]
                 [log4j/log4j "1.2.16"]
                 [com.netflix.curator/curator-framework "1.0.1"]
                 [backtype/jzmq "2.1.0"]
                 [com.googlecode.json-simple/json-simple "1.1"]
                 [com.googlecode/kryo "1.04"]
                 [hiccup "0.3.6"]
                 [org.slf4j/slf4j-log4j12 "1.5.8"]
                 [storm/carbonite "1.0.0"]
                 [org.yaml/snakeyaml "1.9"]
                 [org.apache.httpcomponents/httpclient "4.1.1"]

                 [com.datomic/datomic "0.1.2753"]]

  :plugins [[lein-swank "1.4.4"]
            [lein-pprint "1.1.0"]
            [lein-ring "0.6.2"]]
  
  :profiles {:dev {:dependencies [[clj-stacktrace "0.2.4"]
                                  [swank-clojure "1.3.3"]]}}
  
  :min-lein-version "1.7.0"
  :test-selectors {:default (fn [t] (not (or (:integration t) (:regression t))))
                   :integration :integration
                   :regression :regression
                   :all (fn [t] true)}
  
  :project-init (require 'clojure.pprint)
  :warn-on-reflection true
  
  :repositories {"jboss" "http://repository.jboss.org/nexus/content/groups/public/"
                 "local" ~(str (.toURI (java.io.File. "../mvn_repo")))}
  
  :resources-path "config")