(ns zolo.social.core
  (:use zolodeck.utils.debug))

(def FACEBOOK "FACEBOOK")
(def LINKEDIN "LINKEDIN")
(def TWITTER  "TWITTER")

(defn dispatch-by-provider [params]
  (print-vals "Dispatcher:" params)
  (print-vals "Dispatch value:" (get-in params [:service])))

(defmulti login-user dispatch-by-provider)

(defmethod login-user :default [params]
  (print-vals "LoginUser default:" params)
  (print-vals "LoginUser service:" (get-in params [:service])))