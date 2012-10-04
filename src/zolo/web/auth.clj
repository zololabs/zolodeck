(ns zolo.web.auth
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as conf]
            [zolo.web.fb-auth :as fb-auth]
            [zolo.domain.user :as user]
            [zolodeck.utils.string :as zolo-str]))

(defn fb-user [fb-cookie]
  (let [{user-id :user_id} (fb-auth/decode-signed-request fb-cookie (conf/fb-app-secret))]
    (user/find-by-provider-and-provider-uid :provider/facebook user-id)))

(defn authenticator [req]
  (let [{{fb :value} conf/FB-AUTH-COOKIE-NAME {li :value} conf/LI-AUTH-COOKIE-NAME} (:cookies req)
        user (fb-user fb)]
    (merge {:username (:user/login-provider-uid user)
            :roles #{:user}} (fb-user fb))))