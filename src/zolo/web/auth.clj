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
  ;;Return 404 instead of 403 as we dont want users to know abt this
  ;;resource being there
  {:status 404 :body (json/json-str "Zolo HTTP Authorization Failed.") :headers {}})

(defn- is-owner? [user request]
  ;;TODO No idea why request doesnt have params populated
  (re-find (re-pattern (str "users/" (:user/guid user))) (:uri request)))

(defn- mark-as-owner-if-needed [roles user request]
  (if (is-owner? user request)
    (conj roles :zolo.roles/owner)
    roles))

(defn- get-roles [user request]
  (-> #{:zolo.roles/user}
      (mark-as-owner-if-needed user request)))

(defn authenticate-using-facebook [signed-request request]
  (let [fb-id (-> signed-request
                  (fb-auth/decode-signed-request (conf/fb-app-secret))
                  :user_id)
        user (u-store/find-by-provider-and-provider-uid :provider/facebook fb-id)]
    (when fb-id
      (if user
        (workflows/make-auth {:identity (:user/guid user)
                              :roles (get-roles user request)}
                             {::friend/redirect-on-auth? false})
        (workflows/make-auth {:identity "potential-user"
                              :roles #{:zolo.roles/potential}}
                             {::friend/redirect-on-auth? false})))))

(defn authenticate [{{:strs [authorization]} :headers :as request}]
  (when authorization
    (let [[provider access-token] (-> authorization zstring/decode-base64 (string/split #" "))]
      (if (= provider social/FACEBOOK)
        (authenticate-using-facebook access-token request)))))