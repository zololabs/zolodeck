(ns zolo.web.auth
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as conf]
            [zolo.web.fb-auth :as fb-auth]
            [zolo.domain.user :as user]
            [zolodeck.utils.string :as zolo-str]
            [zolo.utils.logger :as logger]))

;; (defn fb-user [fb-cookie]
;;   (let [{fb-id :user_id} (fb-auth/decode-signed-request fb-cookie (conf/fb-app-secret))]
;;     (logger/debug "Facebook id from facebook signed request: " fb-id)
;;     (user/find-by-provider-and-provider-uid :provider/facebook fb-id)))

;; (defn authenticator [req]
;;   (let [{{fb :value} (conf/fb-auth-cookie-name) {li :value} (conf/li-auth-cookie-name)} (:cookies req)
;;         user (fb-user fb)]
;;     (when user
;;       (logger/debug "Found current user :" (:user/guid user))
;;       (merge {:username (:user/guid user)
;;               :roles #{:user}} user)))
;;   )