(ns zolo.personas.core
  (:use zolodeck.utils.debug
        conjure.core)
  (:require [zolo.marconi.facebook.factory :as fb-factory]
            [zolo.facebook.gateway :as fb-gateway]
            [zolo.marconi.facebook.core :as fb]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.domain.user :as user]
            [clojure.string :as str]))

(defn create-fb-user [firstname lastname]
  (-> (fb/create-user firstname lastname)
      (assoc :auth-token (str firstname "-auth-token"))))

(defn update-fb-friends [fb-user]
  (stubbing [fb-gateway/friends-list (fb/fetch-friends fb-user)]
    (user/update-facebook-friends (:id fb-user))))

(defn update-fb-inbox [fb-user]
  (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages fb-user)]
    (user/update-facebook-inbox (:id fb-user))))

(defn empty-fb-user [first-name last-name]
  (let [u (create-fb-user first-name last-name)]
    (user/insert-fb-user u)
    u))

(defn friend-of [persona friend-first-name]
  (->> persona
       :user/contacts
      (filter #(= (str/lower-case friend-first-name) (str/lower-case (:contact/first-name %))))
      first))