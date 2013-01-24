(ns zolo.personas.factory
  (:use zolodeck.utils.debug
        conjure.core)
  (:require [zolodeck.clj-social-lab.facebook.core :as fb-lab]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.message :as message]))

(defn request-params [fb-user permission-granted?]
  (-> fb-user
      fb-lab/login-creds
      (assoc :provider "FACEBOOK")
      (assoc :permissions_granted permission-granted?)))

(defn fake-extended-user-info [at uid]
  (-> uid
      fb-lab/get-user
      fb-lab/extended-user-info))

(defn fake-friends-list [at uid]
  (-> uid
      fb-lab/get-user
      fb-lab/fetch-friends))

(defn fake-fetch-inbox [at date]
  (-> (fb-lab/current-user)
      fb-lab/fetch-messages))

(defn fake-fetch-feed [& args]
  [])

(defn create-new-db-user
  ([first-name last-name]
     (create-new-db-user first-name last-name true))
  ([first-name last-name permission-granted?]
     (stubbing [fb-gateway/extended-user-info fake-extended-user-info]
       (let [user (fb-lab/create-user first-name last-name)
             params (request-params user permission-granted?)
             cookies {}]
         (-> (social/signup-user params cookies)
             user/signup-new-user
             (user/update-permissions-granted permission-granted?))))))



