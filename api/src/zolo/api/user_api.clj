(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require [zolo.facebook.gateway :as gateway]
            [sandbar.auth :as sandbar]))

(defn upsert-user [request-params]  
  {:user "OK done!"})

(defn friends-list [request-params]
  (gateway/friends-list (:user/fb-auth-token (sandbar/current-user))))


(defmulti contact-strengths :client)

(defmethod contact-strengths "d3" [request-params]
  {"nodes" [{
              "name" "One"
              "group" 1
              }
             {
              "name" "Two"
              "group" 2
              }
             {
              "name" "Three"
              "group" 3
              }
             {
              "name" "Four"
              "group" 2
              }
             
             {
              "name" "Five"
              "group" 3
              }
             ]
   "links" [{
              "source" 0
              "target" 1
              "value" 150
              }
             {
              "source" 0
              "target" 2
              "value" 500
              }		
             {
              "source" 0
              "target" 3
              "value" 100
              }		
             {
              "source" 0
              "target" 4
              "value" 500
              }]
   })

