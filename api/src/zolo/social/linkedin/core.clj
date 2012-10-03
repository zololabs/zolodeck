(ns zolo.social.linkedin.core
  (:use zolodeck.utils.debug)
  (:require [zolo.social.linkedin.users :as users]
            [zolo.setup.config :as conf]
            [clojure.walk :as walk]
            [zolo.social.core :as social]
            [zolo.social.linkedin.token :as token]))

(def AUTH-COOKIE-NAME (str "linkedin_oauth_" (conf/li-api-key)))

;; TODO check for key :oauth_problem in li-at
(defmethod social/login-user social/LINKEDIN [request-params cookies]
  (let [auth-cookie-string (print-vals "auth-cookie:" (get-in cookies [AUTH-COOKIE-NAME :value]))
        li-at (token/access-token auth-cookie-string)]
    (print-vals "LI-AT:" li-at)
    (users/user-and-social-identity li-at)))

(defmethod social/fetch-contacts :provider/linkedin [provider access-token user-id]
  (print-vals "FetchContacts:" provider)
  [])

(defmethod social/fetch-messages :provider/linkedin [provider access-token user-id]
  (print-vals "FetchMessages:" provider)
  [])