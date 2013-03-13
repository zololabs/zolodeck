(ns zolo.social.core
  (:use zolodeck.utils.debug
        [slingshot.slingshot :only [throw+ try+]]))

(def FACEBOOK "FACEBOOK")
(def LINKEDIN "LINKEDIN")
(def TWITTER  "TWITTER")

(def provider-enum {
  FACEBOOK :provider/facebook
  LINKEDIN :provider/linkedin
  TWITTER :provider/twitter})

(def gender-enum {
  "female" :gender/female
  "male"   :gender/male})

(defn provider [request-params]
  (provider-enum (get-in request-params [:login_provider])))

(defn login-dispatcher
  ([params]
     (login-dispatcher params {}))
  ( [params cookies]
      (get-in params [:login_provider])))

(defn provider-dispatcher [provider access-token user-id date]
  provider)

;(def messages-dispatcher pr-dispatcher)

(defmulti provider-uid login-dispatcher)

(defmulti signup-user login-dispatcher)

(defmulti fetch-creds login-dispatcher)

(defmulti fetch-contacts provider-dispatcher)

(defmulti fetch-messages provider-dispatcher)

(defmulti fetch-feed provider-dispatcher)

(defmulti fetch-contact-feeds (fn [provider _ _ _] provider))

(defmethod provider-uid nil [request-params]
  (throw+ {:type :forbidden :message "Provider UID is nil"}))

