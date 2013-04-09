(ns zolo.domain.email-address
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [clojure.string :as string]))

(def NON-PERSON-ID-REGEXES
  (->> ["account"
        "activation"
        "admin"
        "administrator"
        "alert"

        "billing"
        
        "donotreply"
        "do-not-reply"
        "do_not_reply"

        "email"
        
        "feedback"

        "hello"
        "help"

        "info"

        "mail"
        "marketing"
        "member"

        "news"
        "no-reply"
        "no_reply"
        "noreply"
        "notification"        

        "order"
        
        "paypal"

        "registration"
        "root"

        "sales"
        "shipping"
        "support"        
        "system"

        "twitter"

        "webmaster"
        "wordpress"

        "yahoo"
        
        ]
       (map #(str ".*" % ".*"))
       (map #(java.util.regex.Pattern/compile %))))

(def NON-PERSON-DOMAIN-REGEXES
  (->> ["postmaster"
        "emails"
        ]
       (map #(str ".*" % ".*"))
       (map #(java.util.regex.Pattern/compile %))))

(def EMAIL-ADDRESS-BLACKLIST
  ["service@paypal.com"
   "picks@everbrite.com"])

(defn id [email-address]
  (-> email-address
      (string/split #"@")
      first))

(defn domain [email-address]
  (-> email-address
      (string/split #"@")
      last))

(defn split [email-address]
  (-> email-address
      (string/split #"@")))

(defn matches-non-person-signal? [email-address]
  (or
   (some #(re-matches % (id email-address)) NON-PERSON-ID-REGEXES)
   (some #(re-matches % (domain email-address)) NON-PERSON-DOMAIN-REGEXES)
   (some #{email-address} EMAIL-ADDRESS-BLACKLIST)))

(defn remove-non-persons [email-addresses]
  (remove matches-non-person-signal? email-addresses))