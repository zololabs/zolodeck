(ns zolo.social.linkedin.core
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as conf]
            [clojure.walk :as walk]
            [zolo.social.core :as social]
            [zolo.social.linkedin.token :as token]))

(def auth-cookie-name (str "linkedin_oauth_" (conf/li-api-key)))

;; TODO check for key :oauth_problem in li-at
(defmethod social/login-user social/LINKEDIN [request-params cookies]
  (let [auth-cookie-string (get-in cookies [auth-cookie-name :value])
        li-at (token/access-token auth-cookie-string)]
    (print-vals "LI-AT:" li-at)
    
    "LINKED"))