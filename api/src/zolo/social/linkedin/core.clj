(ns zolo.social.linkedin.core
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as conf]
            [clojure.walk :as walk]
            [zolo.social.core :as social]
            [zolo.social.linkedin.token :as token]
            [zolo.social.linkedin.gateway :as gateway]))

(def AUTH-COOKIE-NAME (str "linkedin_oauth_" (conf/li-api-key)))

(def PROFILE-URL "http://api.linkedin.com/v1/people/~")

;; TODO check for key :oauth_problem in li-at
(defmethod social/login-user social/LINKEDIN [request-params cookies]
  (let [auth-cookie-string (get-in cookies [AUTH-COOKIE-NAME :value])
        li-at (token/access-token auth-cookie-string)]
    (print-vals "LI-AT:" li-at)
    (print-vals "Profile:" (gateway/get PROFILE-URL (:oauth_token li-at) (:oauth_token_secret li-at)))
    "LINKED"))