(ns zolo.social.linkedin.gateway
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as conf])
  (:import [org.scribe.builder ServiceBuilder]
           [org.scribe.builder.api LinkedInApi]
           [org.scribe.model OAuthRequest Response Token Verb]
           [org.scribe.oauth OAuthService])  )

(def SERVICE (-> (ServiceBuilder.)
                 (.provider LinkedInApi)
                 (.apiKey (conf/li-api-key))
                 (.apiSecret (conf/li-secret-key))
                 .build))

(defn token-for [token token-secret]
  (Token. token token-secret))

(defn- http-action [url method oauth-token oauth-token-secret]
  (let [req (OAuthRequest. method url)]
    (.addHeader req "x-li-format" "json")    
    (.signRequest SERVICE (token-for oauth-token oauth-token-secret) req)
    (-> req .send .getBody)))

;; (defn post [url]
;;   (let [req (OAuthRequest. Verb/POST url)]
;;     (.addHeader req "x-li-format" "json")    
;;     (.signRequest SERVICE (Token. "" "") req)
;;     (-> req .send .getBody print-vals)))

(defn post
  ([url]
     (post url "" ""))
  ([url oauth-token oauth-token-secret]
     (http-action url Verb/POST oauth-token oauth-token-secret)))

(defn get [url oauth-token oauth-token-secret]
  (http-action url Verb/GET oauth-token oauth-token-secret))