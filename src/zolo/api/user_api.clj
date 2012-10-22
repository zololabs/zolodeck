(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require
   [zolo.social.core :as social]
   [sandbar.auth :as sandbar]
   [zolo.domain.user :as user]
   [zolo.domain.stats :as stats]
   [zolo.social.core :as social-core]
   [zolo.utils.logger :as logger]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user new?]
  {:guid (str (:user/guid user))
   :new new?})

(defn find-user [request-params cookies]
  (user/find-by-provider-and-provider-uid
   (social-core/provider request-params)
   (social/provider-uid request-params cookies)))

(defn signin-user [request-params cookies]
  (if-let [user (find-user request-params cookies)]
    (do
      (logger/debug "User already in system")
      (format-user user false))
    (do
      (logger/debug "New User is getting created with this request params : " request-params)
      (-> request-params
          (social/signup-user cookies)
          user/signup-new-user
          (format-user true)))))

(defn stats [request-params]
  (let [u (user/fully-loaded-user)]
    {:network (stats/network-stats u)
     :other (stats/other-stats u)}))


