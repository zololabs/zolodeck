(ns zolo.personas.factory
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        conjure.core)
  (:require [zolodeck.clj-social-lab.facebook.core :as fb-lab]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.social.facebook.messages :as fb-messages]
            [zolo.social.facebook.stream :as fb-stream]
            [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.message :as message]))

(defn request-params [fb-user permission-granted?]
  (print-vals "request params :" (let [fb-creds (print-vals "Fb Creds :" (fb-lab/login-creds fb-user))]
                                   {:login_provider "FACEBOOK"
                                    :permissions_granted permission-granted?
                                    :access_token (get-in fb-creds [:providerLoginInfo :authResponse :accessToken])
                                    :login_provider_uid (get-in fb-creds [:providerLoginInfo :authResponse :userID])})))

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

(defn fake-extended-access-token [& args]
  (random-str))

(defn fake-fetch-feed [access-token contact-id yyyy-MM-dd-string]
 (fb-lab/fetch-feeds (fb-lab/get-user contact-id)))

(defmacro in-social-lab [& body]
  `(fb-lab/in-facebook-lab
    (stubbing [fb-gateway/extended-user-info fake-extended-user-info
               fb-gateway/friends-list fake-friends-list
               fb-messages/fetch-inbox fake-fetch-inbox
               fb-stream/recent-activity fake-fetch-feed
               fb-gateway/extended-access-token fake-extended-access-token]
      ~@body)))

(defn create-new-db-user
  ([first-name last-name]
     (create-new-db-user first-name last-name true))
  ([first-name last-name permission-granted?]
     (stubbing [fb-gateway/extended-user-info fake-extended-user-info]
       (let [user (fb-lab/create-user first-name last-name)
             params (request-params user permission-granted?)]
         (-> (social/signup-user params)
             user/signup-new-user
             (user/update-permissions-granted permission-granted?))))))



