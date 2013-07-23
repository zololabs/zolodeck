(ns zolo.social.core
  (:use zolo.utils.debug
        [slingshot.slingshot :only [throw+ try+]]))

(def FACEBOOK "FACEBOOK")
(def LINKEDIN "LINKEDIN")
(def TWITTER  "TWITTER")
(def EMAIL "EMAIL")

(def provider-enum {
  FACEBOOK :provider/facebook
  LINKEDIN :provider/linkedin
  TWITTER  :provider/twitter
  EMAIL    :provider/email
})

(defn valid-provider? [provider]
  (some #{provider} (vals provider-enum)))

(def gender-enum {
  "female" :gender/female
  "male"   :gender/male})

(defn provider [request-params]
  (provider-enum (get-in request-params [:login_provider])))

(defn login-dispatcher
  ([params]
     (login-dispatcher nil params))
  ([guid params]
     (get-in params [:login_provider])))

(defn provider-dispatcher [provider access-token user-id date]
  provider)

;(def messages-dispatcher pr-dispatcher)

(defmulti provider-uid login-dispatcher)

(defmulti fetch-user-identity login-dispatcher)

(defmulti fetch-creds login-dispatcher)

(defmulti fetch-social-identities provider-dispatcher)

(defmulti fetch-messages provider-dispatcher)

(defmulti fetch-deleted-messages provider-dispatcher)

(defmulti fetch-feed provider-dispatcher)

(defmulti fetch-contact-feeds (fn [provider _ _ _] provider))

(defmethod provider-uid nil [request-params]
  (throw+ {:type :forbidden :message "Provider UID is nil"}))

(defn message-provider-dispatch [provider a_ f_ to_ th_ r_ s_ m_]
  provider)

(defmulti send-message message-provider-dispatch)