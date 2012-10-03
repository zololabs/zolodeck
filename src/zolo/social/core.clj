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
  "male" :gender/male})

(defn login-dispatcher [params cookies]
  (print-vals "Dispatcher:" params)
  (print-vals "Dispatch value:" (get-in params [:provider])))

(defmulti login-user login-dispatcher)

(defmethod login-user :default [params cookies]
  (print-vals "LoginUser default:" params)
  (print-vals "LoginUser service:" (get-in params [:provider])))

(defn contacts-dispatcher [provider access-token user-id]
  provider)

(defmulti fetch-contacts contacts-dispatcher)

(def messages-dispatcher contacts-dispatcher)

(defmulti fetch-messages messages-dispatcher)