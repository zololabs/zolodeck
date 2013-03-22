(ns zolo.personas.shy
  (:use zolodeck.utils.debug
        conjure.core
        zolodeck.demonic.core)
  (:require [zolodeck.clj-social-lab.facebook.core :as fb-lab]
            [zolo.domain.contact :as contact]
            [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]))

(defn email []
  "Shy.Hal@gmail.com")

(defn create []
  (personas/in-social-lab
   (let [shy (fb-lab/create-user "Shy" "Hal")
         jack (fb-lab/create-friend "Jack" "Daniels")
         jill (fb-lab/create-friend  "Jill" "Ferry")
         d-shy (personas/create-domain-user shy)]
     
       (fb-lab/make-friend shy jack)
       (fb-lab/make-friend shy jill)
       
       (fb-lab/login-as shy)

       d-shy
       ;;(contact/update-contacts d-shy)
       )))