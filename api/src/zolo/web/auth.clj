(ns zolo.web.auth
  (:use zolo.utils.debug)
  (:require [zolo.facebook.gateway :as facebook]
            [zolo.domain.user :as user]
            [zolo.utils.string :as zolo-str]))

(defmulti authenticate (fn [auth-type auth-cred params] 
                         (clojure.string/lower-case (clojure.string/trim auth-type))))

(defmethod authenticate "fb" [_ auth-cred params]
  (if-let [signed-request (facebook/decode-signed-request auth-cred)]
    (user/find-by-fb-signed-request signed-request)))

(defmethod authenticate :default [_ _ _]
  nil)

(defn authenticator [req]
  (if-let [auth-token ((:headers req) "authorization")]
    (let [[auth-type auth-cred] (zolo-str/split " " auth-token)]
      (if-let [user (authenticate auth-type auth-cred (:params req))]
        (merge {:username (:email user)
                :roles #{:user}}
               user)))))