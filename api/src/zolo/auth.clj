(ns zolo.auth
  (:use zolo.utils.debug)
  (:require [zolo.ext.facebook :as facebook]
            [zolo.utils.string :as zolo-str]))

(defmulti authenticate (fn [auth-type auth-cred params] (clojure.string/lower-case (clojure.string/trim auth-type))))

(defmethod authenticate "fb" [_ auth-cred params]
  (if-let [signed-request (facebook/decode-signed-request auth-cred)]
    {:email "sova@goo.com"
     :signed-request signed-request}))

(defmethod authenticate :default [_ _ _]
  nil)

(defn authenticator [req]
  (if-let [auth-token ((:headers req) "authorization")]
    (let [[auth-type auth-cred] (zolo-str/split " " auth-token)]
          (if-let [user (authenticate auth-type auth-cred (:params req))]
            (merge user
                   {:username (:email user)
                    :roles #{:user}})))))