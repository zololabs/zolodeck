(ns zolo.personas.loner
  (:use zolodeck.utils.debug
        conjure.core)
  (:require [zolo.marconi.facebook.factory :as fb-factory]
            [zolo.facebook.gateway :as fb-gateway]
            [zolo.marconi.facebook.core :as fb]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.domain.user :as user]
            [zolo.personas.core :as personas]))

(defn create []
  (fb/in-facebook-lab
   (let [loner (-> (personas/create-fb-user "Loner" "Hal"))]
     (user/insert-fb-user loner)
     
     (personas/update-fb-friends loner)
     (personas/update-fb-inbox loner)
     
     (user/find-by-fb-id (:id loner)))))