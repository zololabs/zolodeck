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

        "customerservice"
        
        "donotreply"
        "do-not-reply"
        "do_not_reply"

        "email"
        
        "feedback"

        "hello"
        "help"

        "info"

        "mail"
        "mailer"
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
        "services"
        "summary"

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
        "facebookmail.com"
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
  (let [[i d] (split email-address)]
    (boolean (or
              (some #(re-matches % i) NON-PERSON-ID-REGEXES)
              (some #(re-matches % d) NON-PERSON-DOMAIN-REGEXES)
              (some #{email-address} EMAIL-ADDRESS-BLACKLIST)))))

(defn remove-non-persons [email-addresses]
  (remove matches-non-person-signal? email-addresses))