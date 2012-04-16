(ns zolo.setup.datomic-schema
  (:use zolodeck.demonic.schema))

(def SCHEMA-TX {
     000 (string-fact-schema :user/first-name true "A user's first name") 

     001 (string-fact-schema :user/last-name true "A user's last name") 

     002 (string-fact-schema :user/gender false "A user's gender") 

     ; Facebook Information

     003 (string-fact-schema :user/fb-id false "A user's Facebook ID") 

     004 (string-fact-schema :user/fb-auth-token false "A user's Facebook auth token") 

     005 (string-fact-schema :user/fb-email false "A user's Facebook email") 

     006 (string-fact-schema :user/fb-link false "A user's Facebook link") 

     007 (string-fact-schema :user/fb-username false "A user's Facebook username") 

})