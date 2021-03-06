(ns zolo.social.linkedin.core
  (:use zolo.utils.debug)
  (:require [zolo.social.linkedin.users :as users]
            [zolo.social.linkedin.contacts :as contacts]
            [zolo.social.linkedin.gateway :as gateway]
            [zolo.setup.config :as conf]
            [clojure.walk :as walk]
            [zolo.social.core :as social]
            [zolo.social.linkedin.token :as token]
            [zolo.utils.logger :as logger]))

(defmethod social/provider-uid social/LINKEDIN [request-params]
  ;; (-> (get-in cookies [(conf/li-auth-cookie-name) :value])
  ;;     token/parse-oauth-cookie
  ;;     :member_id)
  )

;; TODO check for key :oauth_problem in li-at
(defmethod social/fetch-user-identity social/LINKEDIN [request-params cookies]
  (let [auth-cookie-string (get-in cookies [(conf/li-auth-cookie-name) :value])
        parsed-cookie (token/parse-oauth-cookie auth-cookie-string)
        li-at (token/access-token (:access_token parsed-cookie))]
    (logger/info "LI-AT:" li-at)
    (users/user-identity li-at)))

(defmethod social/fetch-social-identities :provider/linkedin [provider access-token user-id]
  (let [{oauth-token :oauth_token oauth-token-secret :oauth_token_secret} (read-string access-token)
        contacts (gateway/contacts-list oauth-token oauth-token-secret)]
    (doall (map contacts/contact-object contacts))))

(defmethod social/fetch-messages :provider/linkedin [provider access-token user-id]
  (logger/info "FetchMessages Not Support:" provider)
  [])