(ns zolo.web.auth
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [zolo.web.fb-auth :as fb-auth]
            [zolo.store.user-store :as u-store]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [zolo.utils.string :as zstring]
            [zolo.utils.logger :as logger]
            [zolo.social.core :as social]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(defn current-user []
  friend/*identity*)

(defn return-forbidden [request]
  {:status 403 :body (json/json-str "Zolo HTTP Authorization Failed.") :headers {}})

(defn authenticate-using-facebook [signed-request]
  (let [fb-id (-> signed-request
                  (fb-auth/decode-signed-request (conf/fb-app-secret))
                  :user_id)]
    (if-let [user (u-store/find-by-provider-and-provider-uid :provider/facebook fb-id)]
      (workflows/make-auth {:identity (:user/guid user)
                            :roles #{:zolo.roles/user}}
                           {::friend/redirect-on-auth? false}))))

(defn authenticate [{{:strs [authorization]} :headers :as request}]
  (when authorization
    (let [[provider access-token] (-> authorization zstring/decode-base64 (string/split #" "))]
      (if (= provider social/FACEBOOK)
        (authenticate-using-facebook access-token)))))