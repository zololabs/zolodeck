(ns zolo.social.linkedin.token
  (:use zolodeck.utils.debug)
  (:require [clojure.data.json :as json]
            [zolodeck.utils.string :as string]
            [zolo.setup.config :as conf])
  (:import [org.scribe.builder ServiceBuilder]
           [org.scribe.builder.api LinkedInApi]
           [org.scribe.model OAuthRequest Response Token Verb]
           [org.scribe.oauth OAuthService]))

(def EXCHANGE_URL "https://api.linkedin.com/uas/oauth/accessToken")

(defn- parse-oauth-cookie [cookie-value]
  (-> cookie-value
      json/read-json))

(def service (-> (ServiceBuilder.)
                 (.provider LinkedInApi)
                 (.apiKey (conf/li-api-key))
                 (.apiSecret (conf/li-secret-key))
                 .build))

(defn access-token [oauth-cookie-string]
  (let [c (parse-oauth-cookie oauth-cookie-string)
        xoauth-oauth2-access-token (:access_token c)
        token (Token. "" "")
        req (OAuthRequest. Verb/POST (str EXCHANGE_URL "?xoauth_oauth2_access_token=" xoauth-oauth2-access-token))
        _ (.signRequest service token req)
        resp (.send req)]
    (-> resp .getBody (string/parse-query-string "UTF-8"))))