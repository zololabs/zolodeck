(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require
   [zolo.social.core :as social]
   [sandbar.auth :as sandbar]
   [zolo.domain.user :as user]
   [zolo.domain.contact :as contact]
   [zolo.domain.social-identity :as social-identity]
   [zolo.domain.user-identity :as user-identity]
   [zolo.domain.message :as message]
   [zolo.domain.interaction :as interaction]
   [zolo.domain.accessors :as dom]   
   [zolo.stats.activity :as activity]
   [zolo.social.facebook.chat :as fb-chat]
   [zolo.social.core :as social-core]
   [zolo.utils.logger :as logger]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user new?]
  {:guid (str (:user/guid user))
   :email (user-identity/fb-email user)
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
      (user/update-creds user (social/fetch-creds request-params cookies))
      (log-into-fb-chat user)
      (format-user user false))
    (do
      (logger/debug "New User is getting created with this request params : " request-params)
      (-> request-params
          (social/signup-user cookies)
          user/signup-new-user
          user/update-with-extended-fb-auth-token          
          log-into-fb-chat
          (format-user true)))))

(defn empty-stats []
  {:network {} :other {} :recent []})

(defn user-stats [u client-date]
  (if (user/been-processed? u)
    (let [imbc (dom/inbox-messages-by-contacts u)
          ibc (interaction/interactions-by-contacts imbc)]
      {:network (activity/network-stats u imbc)
       :other (activity/other-stats u ibc client-date)
       :recent (activity/recent-activity u)
       :interactions (activity/daily-counts-for-network ibc)})
    (empty-stats)))

(defn send-message [request-params]
  (let [{provider :provider contact-guid :to-uid text :text thread-id :thread_id} request-params
        from-uid (-> (current-user) user-identity/fb-id)
        to-uid (-> contact-guid contact/find-by-guid-string social-identity/fb-id)]
    (fb-chat/send-message from-uid to-uid text)
    (message/create-new (user/current-user) provider to-uid text thread-id)
    (user-stats (user/reload (user/current-user)))))

(defn stats [request-params]
  (user-stats (user/current-user) (:client-date request-params)))


