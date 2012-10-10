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

(defn login-dispatcher [params cookies]
  (get-in params [:provider]))

(defn contacts-dispatcher [provider access-token user-id]
  provider)

(def messages-dispatcher contacts-dispatcher)

(defmulti provider-uid login-dispatcher)

(defmulti signup-user login-dispatcher)

(defmulti fetch-contacts contacts-dispatcher)

(defmulti fetch-messages messages-dispatcher)

