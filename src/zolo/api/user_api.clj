(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require
   [zolo.social.core :as social]
   [sandbar.auth :as sandbar]
   [zolo.domain.user :as user]
   [zolo.domain.message :as message]   
   [zolo.domain.stats :as stats]
   [zolo.social.facebook.chat :as fb-chat]
   [zolo.social.core :as social-core]
   [zolo.utils.logger :as logger]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user new?]
  {:guid (str (:user/guid user))
   :new new?})

(defn log-into-fb-chat [user]
  (future
    (fb-chat/connect-user! user)
    nil)
  user)

(defn find-user [request-params cookies]
  (user/find-by-provider-and-provider-uid
   (social-core/provider request-params)
   (social/provider-uid request-params cookies)))

(defn signin-user [request-params cookies]
  (if-let [user (find-user request-params cookies)]
    (do
      (logger/debug "User already in system")
      (log-into-fb-chat user)
      (format-user user false))
    (do
      (logger/debug "New User is getting created with this request params : " request-params)
      (-> request-params
          (social/signup-user cookies)
          user/signup-new-user
          log-into-fb-chat
          (format-user true)))))

(defn send-message [request-params]
  (let [{provider :provider to-uid :to_uid text :text thread-id :thread_id} request-params]
    (message/create-new (user/current-user) provider to-uid text thread-id)))

(defn stats [request-params]
  (let [u (user/fully-loaded-user)]
    (if (user/been-processed? u)
      {:network (stats/network-stats u)
       :other (stats/other-stats u)
       :recent (stats/recent-activity u)}
      {:network {} :other {} :recent []})))


