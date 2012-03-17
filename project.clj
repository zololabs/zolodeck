(defproject zolodeck "0.1.0-SNAPSHOT"
  :description "Zolodeck"
  :dev-resources-path "script"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.6.2"]
                 [ring/ring-core "0.3.7"]
                 [ring/ring-jetty-adapter "0.2.3"]
                 [sandbar/sandbar "0.3.0"]
                 [clj-base64 "0.0.1"]
                 [org.bituf/clj-stringtemplate "0.2"]
                 [org.slf4j/slf4j-log4j12 "1.6.1"]
                 [clj-facebook-graph "0.2.0"]
                 [bouncycastle/bcprov-jdk16-nosign "140"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [clj-http "0.1.3"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :resources-path "config")