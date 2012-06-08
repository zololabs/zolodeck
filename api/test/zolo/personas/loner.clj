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
   (let [loner (fb/create-user "Loner" "Fong")]
     (user/insert-fb-user loner)

     (user/find-by-fb-id (:id loner)))))