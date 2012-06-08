(ns zolo.personas.loner
  (:use zolodeck.utils.debug
        conjure.core)
  (:require [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
            [zolo.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.domain.user :as user]))

(defn create []
  (fb/in-facebook-lab
   (-> (fb/create-user "Loner" "Fong")
       (merge {:auth-token "loner-auth-token"})
       user/insert-fb-user
       :user/fb-id
       user/find-by-fb-id)))