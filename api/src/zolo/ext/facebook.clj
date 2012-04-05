(ns zolo.ext.facebook
  (:use zolo.utils.debug)
  (:require [clj-facebook-graph.auth :as fb-auth]
            [clj-facebook-graph.client :as fb-client]))


(def APP-ID "361942873847116")
(def APP-SECRET "6a968bdeb2eb92ac913ec1b88f88cef6")

(def facebook-oauth2
  {:authorization-uri "https://graph.facebook.com/oauth/authorize"
   :access-token-uri "https://graph.facebook.com/oauth/access_token"
   :client-id APP-ID
   :client-secret APP-SECRET
   :access-query-param :access_token
   :grant-type 'authorization-code})

(defn decode-signed-request [encoded-signed-request] 
  (fb-auth/decode-signed-request encoded-signed-request APP-SECRET))

(defn code->token [code]
  (fb-auth/get-access-token facebook-oauth2 {:code code}))

(defn friends-list [auth-token]
  (fb-auth/with-facebook-auth {:access-token auth-token} 
    (fb-client/get [:me :friends]
                   {:query-params {:fields "link,name,gender,bio,birthday,relationship_status,significant_other,website"} 
                    :extract :data 
                    :paging true})))