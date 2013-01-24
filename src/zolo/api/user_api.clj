(ns zolo.api.user-api
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.social.core :as social]
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
            [zolo.utils.logger :as logger]
            [zolodeck.utils.calendar :as zolo-cal]))

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

(defn- update-user-creds [user request-params cookies]
  (user/update-creds user (social/fetch-creds request-params cookies))
  (user/update-permissions-granted user (:permissions_granted request-params))
  (log-into-fb-chat user)
  (format-user user false))

(defn signin-user [request-params cookies]
  (if-let [user (find-user request-params cookies)]
    (do
      (logger/debug "User already in system")
      (update-user-creds user request-params cookies))
    (do
      (logger/debug "New User is getting created with this request params : " request-params)
      (-> request-params
          (social/signup-user cookies)
          user/signup-new-user
          user/update-with-extended-fb-auth-token          
          log-into-fb-chat
          (format-user true)))))

(defn update-user [request-params cookies]
  (update-user-creds (user/current-user) request-params cookies))

(defn client-date [request-params]
  (-> request-params :client-tz parse-int (zolo-cal/now-joda)))

(defn empty-stats []
  {:network {} :other {} :recent []})

(defn user-stats [u client-date]
  (if (user/been-processed? u)
    (let [imbc (dom/inbox-messages-by-contacts u)
          ibc (interaction/interactions-by-contacts imbc)]
      {:network (activity/network-stats u imbc)
       :other (activity/other-stats u ibc client-date)
       ;; :recent (activity/recent-activity u)
       :interactions (activity/daily-counts-for-network ibc)})
    (empty-stats)))

(defn send-message [request-params]
  (let [{provider :provider contact-guid :to-uid text :text thread-id :thread_id} request-params 
        to-uid (-> contact-guid contact/find-by-guid-string social-identity/fb-id)]
    (fb-chat/send-message (user/current-user) to-uid text)
    (message/create-new (user/current-user) provider to-uid text thread-id)
    (-> (user/current-user)
        user/reload
        (user-stats (client-date request-params))
        (assoc :to-uid contact-guid))))

(defn stats [request-params]
  (user-stats (user/current-user) (client-date request-params)))


