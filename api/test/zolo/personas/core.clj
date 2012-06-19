(ns zolo.personas.core
  (:use zolodeck.utils.debug
        conjure.core)
  (:require [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
            [zolo.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.domain.user :as user]))

(defn create-fb-user [firstname lastname]
  (-> (fb/create-user firstname lastname)
      (assoc :auth-token (str firstname "-auth-token"))))

(defn update-fb-friends [fb-user]
  (stubbing [fb-gateway/friends-list (fb/fetch-friends fb-user)]
    (user/update-facebook-friends (:id fb-user))))

(defn update-fb-inbox [fb-user]
  (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages fb-user)]
    (user/update-facebook-inbox (:id fb-user))))

(defn friend-of [persona friend-first-name]
  (->> persona
       :user/contacts
       ;;TODO look for lowercase function
      (filter #(= (.toLowerCase friend-first-name) (.toLowerCase (:contact/first-name %))))
      first))