(defproject com.currylogic/zolodeck-api "0.1.0-SNAPSHOT"
  :description "Zolodeck API"

  :dependencies [[org.clojure/clojure "1.4.0"]

                 [compojure "1.0.2" :exclude [org.clojure/clojure]]
                 [ring "1.0.2" :exclude [org.clojure/clojure]]

                 [fuziontech/ring-json-params "0.2.0" :exclude [org.clojure/clojure]]
                 
                 [sandbar/sandbar "0.4.0-SNAPSHOT" :exclude [org.clojure/clojure]]

                 [org.clojure/data.json "0.1.2"  :exclude [org.clojure/clojure]]
                 [clj-http "0.5.3"]

                 [joda-time "1.6"]
                 [clj-time "0.3.7"]
                 [slingshot "0.10.2"]

                 [org.clojars.nakkaya.javax.mail/imap "1.4.3"]
                 [org.clojars.nakkaya.javax.mail/mail "1.4.3"]

                 [org.clojars.amit/zolo_fb_chat "0.0.1"]
                 
                 [org.clojure/core.match "0.2.0-alpha9"]

                 [org.scribe/scribe "1.3.2"] ;;oauth for LinkedIn
                 
                 [zolodeck/demonic "0.1.0-SNAPSHOT"]
                 [zolodeck/zolo-utils "0.1.0-SNAPSHOT"]
                 [world-country-list "1.0.0-SNAPSHOT"]]

  :plugins [[lein-swank "1.4.4"]
            [lein-pprint "1.1.1"]
            [lein-ring "0.6.2"]
            [lein-difftest "1.3.8"]
            [lein-notes "0.0.1"]
            [lein-deploy-app "0.1.0"]]

  :hooks [leiningen.hooks.difftest]

  :dev-dependencies [[clj-stacktrace "0.2.4"]
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
                    (use 'zolo.test.core-utils)
                    (require '[zolo.setup.datomic-setup :as datomic-setup])
                    (datomic-setup/init-datomic))

  :warn-on-reflection false
  
  :repositories {"jboss" "http://repository.jboss.org/nexus/content/groups/public/"
                 "local" ~(str (.toURI (java.io.File. "mvn_repo")))}
  
  :resources-path "config"

  :deploy-app {:s3-bucket "s3p://zolodeck/releases/"
               :creds :env}

  :main zolo.core)