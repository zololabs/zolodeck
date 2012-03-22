(defproject zolodeck "0.1.0-SNAPSHOT"
  :description "Zolodeck"
  :repositories {"jboss" "http://repository.jboss.org/nexus/content/groups/public/"
                 "local" ~(str (.toURI (java.io.File. 
                                 (str (System/getProperty "user.name") "/.zolo.mvn"))))}
  :dev-resources-path "script"
  :dependencies [[org.clojure/clojure "1.4.0-beta3"]
;                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.6.2"]
                 [ring/ring-core "0.3.7"]
                 [ring/ring-jetty-adapter "0.2.3"]
                 [sandbar/sandbar "0.3.0"]
                 [clj-base64 "0.0.1"]
                 [org.bituf/clj-stringtemplate "0.2"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [clj-facebook-graph "0.2.0"]
                 [bouncycastle/bcprov-jdk16-nosign "140"]
                 [clj-http "0.1.3"]
                 [joda-time "1.6"]

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
                 [compojure "0.6.4"]
                 [hiccup "0.3.6"]
                 [ring/ring-jetty-adapter "0.3.11"]
                 [org.slf4j/slf4j-log4j12 "1.5.8"]
                 [storm/carbonite "1.0.0"]
                 [org.yaml/snakeyaml "1.9"]
                 [org.apache.httpcomponents/httpclient "4.1.1"]

                 [datomic/peer "0.1.2678"]

]
  :plugins [[lein-swank "1.4.3"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :resources-path "config")