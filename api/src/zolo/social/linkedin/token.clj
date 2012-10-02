(ns zolo.social.linkedin.token
  (:require [ring.util.codec :as codec]
            [clojure.data.json :as json]
            [clojure.string :as string])
  (:import [org.scribe.builder ServiceBuilder]
           [org.scribe.builder.api LinkedInApi]
           [org.scribe.model OAuthRequest Response Token Verb]
           [org.scribe.oauth OAuthService]))

(def EXCHANGE_URL "https://api.linkedin.com/uas/oauth/accessToken")

(defn- parse-oauth-cookie [cookie-value]
  (-> cookie-value
      codec/url-decode
      json/read-json))

(def service (-> (ServiceBuilder.)
                 (.provider LinkedInApi)
                 (.apiKey "p8rw9l9pzvl8")
                 (.apiSecret "1thkgbvKwrcFdZ7N")
                 .build))

(defn- at-params [decoded-cookie]
  {:oauth_consumer_key "p8rw9l9pzvl8"
   :xoauth_oauth2_access_token (:access_token decoded-cookie)
   :oauth_signature_method "HMAC-SHA1"
   :oauth_signature (-> decoded-cookie :signature (codec/url-encode "UTF-8"))})


(defn- assoc-param
  "Associate a key with a value. If the key already exists in the map,
  create a vector of values."
  [map key val]
  (assoc map key
    (if-let [cur (map key)]
      (if (vector? cur)
        (conj cur val)
        [cur val])
      val)))

(defn- parse-params
  "Parse parameters from a string into a map."
  [^String param-string encoding]
  (reduce
    (fn [param-map encoded-param]
      (if-let [[_ key val] (re-matches #"([^=]+)=(.*)" encoded-param)]
        (assoc-param param-map
          (codec/url-decode key encoding)
          (codec/url-decode (or val "") encoding))
         param-map))
    {}
    (string/split param-string #"&")))

(defn access-token [cookie-value]
  (let [c (parse-oauth-cookie cookie-value)
        params (at-params c)
        token (Token. "" "")
        req (OAuthRequest. Verb/POST (str EXCHANGE_URL "?xoauth_oauth2_access_token="  (params :xoauth_oauth2_access_token)))
        _ (.signRequest service token req)
        resp (.send req)]
    (-> resp .getBody (parse-params "UTF-8"))))