(ns zolo.social.linkedin.gateway
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [clojure.walk :as walk]
            [clojure.data.json :as json])
  (:import [org.scribe.builder ServiceBuilder]
           [org.scribe.builder.api LinkedInApi]
           [org.scribe.model OAuthRequest Response Token Verb]
           [org.scribe.oauth OAuthService])  )

(defn service []
  (-> (ServiceBuilder.)
      (.provider LinkedInApi)
      (.apiKey (conf/li-api-key))
      (.apiSecret (conf/li-secret-key))
      .build))

(def PROFILE-URL "http://api.linkedin.com/v1/people/~:(id,first-name,last-name,industry,location:(country:(code)),date-of-birth,email-address,picture-url,public-profile-url)")

(def CONTACTS-URL "http://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,industry,location:(country:(code)),date-of-birth,email-address,picture-url,public-profile-url)")

(defn token-for [token token-secret]
  (Token. token token-secret))

(defn- http-action [url method oauth-token oauth-token-secret]
  (let [req (OAuthRequest. method url)]
    (.addHeader req "x-li-format" "json")    
    (.signRequest (service) (token-for oauth-token oauth-token-secret) req)
    (-> req .send .getBody)))

(defn http-post
  ([url]
     (http-post url "" ""))
  ([url oauth-token oauth-token-secret]
     (http-action url Verb/POST oauth-token oauth-token-secret)))

(defn http-get [url oauth-token oauth-token-secret]
  (http-action url Verb/GET oauth-token oauth-token-secret))

(defn get-json [url oauth-token oauth-token-secret]
  (-> (http-get url oauth-token oauth-token-secret)
      json/read-json
      walk/keywordize-keys))

(defn profile-info [oauth-token oauth-token-secret]
  (get-json PROFILE-URL oauth-token oauth-token-secret))

;; TODO - use modified-since parameter to get only new/updated contacts
(defn contacts-list [oauth-token oauth-token-secret]
  (-> (get-json CONTACTS-URL oauth-token oauth-token-secret)
      :values))