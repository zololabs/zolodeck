(ns zolo.setup.datomic-schema
  (:use zolodeck.demonic.schema))

(def USER-SCHEMA-TX 
     [(string-fact-schema :user/first-name true "A user's first name") 
      (string-fact-schema :user/last-name true "A user's last name") 
      (string-fact-schema :user/gender false "A user's gender") 
                                        ;Facebook Information
      (string-fact-schema :user/fb-id false "A user's Facebook ID") 
      (string-fact-schema :user/fb-auth-token false "A user's Facebook auth token") 
      (string-fact-schema :user/fb-email false "A user's Facebook email") 
      (string-fact-schema :user/fb-link false "A user's Facebook link") 
      (string-fact-schema :user/fb-username false "A user's Facebook username")
                                        ; Contacts Information
      (refs-fact-schema :user/contacts true "A user's contacts")])


(def CONTACT-SCHEMA-TX 
     [(string-fact-schema :contact/first-name true "A contact's first name") 
      (string-fact-schema :contact/last-name true "A contact's last name") 
      (string-fact-schema :contact/gender false "A contact's gender") 
      ;Facebook Information
      (string-fact-schema :contact/fb-id false "A contact's Facebook ID") 
      (string-fact-schema :contact/fb-email false "A contact's Facebook email") 
      (string-fact-schema :contact/fb-link false "A contact's Facebook link") 
      (string-fact-schema :contact/fb-username false "A contact's Facebook username")])

(def SCHEMA-TX
     (concat USER-SCHEMA-TX CONTACT-SCHEMA-TX))