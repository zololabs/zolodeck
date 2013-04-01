(ns zolo.personas.shy
  (:use zolodeck.utils.debug
        conjure.core
        zolodeck.demonic.core)
  (:require [zolo.marconi.facebook.core :as fb-lab]
            [zolo.domain.contact :as contact]
            [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.service.user-service :as u-service]))

(defn create []
  (personas/in-social-lab
   (let [shy (fb-lab/create-user "Shy" "Hal")
         jack (fb-lab/create-friend "Jack" "Daniels")
         jill (fb-lab/create-friend  "Jill" "Ferry")
         db-shy (personas/create-db-user shy)]
     
       (fb-lab/make-friend shy jack)
       (fb-lab/make-friend shy jill)
       
       (fb-lab/login-as shy)

       (-> db-shy
           u-service/refresh-user-data
           u-service/refresh-user-scores))))