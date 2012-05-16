(ns zolo.facebook.gateway
  (:use zolodeck.utils.debug)
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
   :grant-type "authorization_code"})

(defn decode-signed-request [encoded-signed-request] 
  (fb-auth/decode-signed-request encoded-signed-request APP-SECRET))

(defn code->token [code]
  (fb-auth/get-access-token facebook-oauth2 {:code code}))

(defn me [auth-token]
  (assoc 
      (:body (fb-auth/with-facebook-auth {:access-token auth-token} 
               (fb-client/get [:me])))
    :auth-token auth-token))

(defn friends-list [auth-token]
  (fb-auth/with-facebook-auth {:access-token auth-token} 
    (fb-client/get [:me :friends]
                   {:query-params 
                    {:fields "id,first_name,last_name,gender,locale,link,username,installed,bio,birthday,education,email,hometown,interested_in,location,picture,relationship_status,significant_other,website"}
                    :extract :data 
                    :paging true})))

(defn run-fql [auth-token query-string]
  (:body (fb-auth/with-facebook-auth {:access-token auth-token} 
           (fb-client/get :fql
                          {:fql query-string}))))
