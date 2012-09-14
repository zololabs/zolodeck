(ns zolo.web.auth
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.user :as user]
            [zolodeck.utils.string :as zolo-str]))

(defmulti authenticate (fn [auth-type auth-cred params] 
                         (clojure.string/lower-case (clojure.string/trim auth-type))))

(defmethod authenticate "bearer" [_ auth-cred params]
  (print-vals "Loaded User:" (user/find-by-guid-string auth-cred)))

(defmethod authenticate :default [_ _ _]
  nil)

(defn authenticator [req]
  (if-let [auth-token ((:headers req) "authorization")]
    (let [[auth-type auth-cred] (zolo-str/split " " auth-token)]
      (if-let [user (authenticate auth-type auth-cred (:params req))]
        (merge {:username (:user/login-provider-uid user)
                :roles #{:user}}
               user)))))