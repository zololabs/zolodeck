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

(defn dispatch-by-provider [params]
  (print-vals "Dispatcher:" params)
  (print-vals "Dispatch value:" (get-in params [:service])))

(defmulti login-user dispatch-by-provider)

(defmethod login-user :default [params]
  (print-vals "LoginUser default:" params)
  (print-vals "LoginUser service:" (get-in params [:service])))