(ns zolo.social.core
  (:use zolodeck.utils.debug))

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
  (provider-enum (get-in request-params [:provider])))

(defn login-dispatcher [params cookies]
  (get-in params [:provider]))

(defn provider-dispatcher [provider access-token user-id]
  provider)

;(def messages-dispatcher pr-dispatcher)

(defmulti provider-uid login-dispatcher)

(defmulti signup-user login-dispatcher)

(defmulti fetch-contacts provider-dispatcher)

(defmulti fetch-messages provider-dispatcher)

(defmulti fetch-feed provider-dispatcher)