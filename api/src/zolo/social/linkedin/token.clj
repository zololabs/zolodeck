(ns zolo.social.linkedin.token
  (:use zolodeck.utils.debug)
  (:require [zolo.social.linkedin.gateway :as gateway]
            [clojure.data.json :as json]
            [zolodeck.utils.string :as string]
            [zolo.setup.config :as conf]))

(def EXCHANGE_URL "https://api.linkedin.com/uas/oauth/accessToken")

(defn- parse-oauth-cookie [cookie-value]
  (-> cookie-value
      json/read-json))

(defn access-token [oauth-cookie-string]
  (let [c (parse-oauth-cookie oauth-cookie-string)
        xoauth-oauth2-access-token (:access_token c)
        url (str EXCHANGE_URL "?xoauth_oauth2_access_token=" xoauth-oauth2-access-token)
        response (gateway/post url)]
    (print-vals "Parsed->" (string/parse-query-string response "UTF-8"))))