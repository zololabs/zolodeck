(ns zolo.social.linkedin.core
  (:use zolodeck.utils.debug)
  (:require [zolo.social.linkedin.users :as users]
            [zolo.social.linkedin.contacts :as contacts]
            [zolo.social.linkedin.gateway :as gateway]
            [zolo.setup.config :as conf]
            [clojure.walk :as walk]
            [zolo.social.core :as social]
            [zolo.social.linkedin.token :as token]))

(defmethod social/provider-uid social/LINKEDIN [request-params cookies]
  (-> (get-in cookies [(conf/li-auth-cookie-name) :value])
      token/parse-oauth-cookie
      :member_id))

;; TODO check for key :oauth_problem in li-at
(defmethod social/signup-user social/LINKEDIN [request-params cookies]
  (let [auth-cookie-string (get-in cookies [(conf/li-auth-cookie-name) :value])
        parsed-cookie (token/parse-oauth-cookie auth-cookie-string)
        li-at (token/access-token (:access_token parsed-cookie))]
    (print-vals "LI-AT:" li-at)
    (users/user-and-social-identity li-at)))

(defmethod social/fetch-contacts :provider/linkedin [provider access-token user-id]
  (let [{oauth-token :oauth_token oauth-token-secret :oauth_token_secret} (read-string access-token)
        contacts (gateway/contacts-list oauth-token oauth-token-secret)]
    (doall (map contacts/contact-object contacts))))

(defmethod social/fetch-messages :provider/linkedin [provider access-token user-id]
  (print-vals "FetchMessages Not Support:" provider)
  [])