(ns zolo.personas.shy
  (:use zolodeck.utils.debug
        conjure.core)
  (:require [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
            [zolo.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.domain.user :as user]
            [zolo.personas.core :as personas]))

(defn create []
  (fb/in-facebook-lab
   (let [shy (personas/create-fb-user "Shy" "Hal")
         jack (personas/create-fb-user "Jack" "Daniels")
         jill (personas/create-fb-user "Jill" "Ferry")]
     (user/insert-fb-user shy)

     (fb/make-friend shy jack)
     (fb/make-friend shy jill)

     (personas/update-fb-friends shy)
     (personas/update-fb-inbox shy)

     (:user/contacts (user/find-by-fb-id (:id shy)))

     (user/find-by-fb-id (:id shy)))))