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

;;TODO Dummy functions which returns JSON that D3 lib needs
(defn- node [no]
  {"name" (str "Friend-" no)
   "group" (rand-int 10)})

(defn- link [no]
  {"source" 0
   "target" no
   "value" (rand-int 200)})

(defmethod contact-strengths "d3" [request-params]
  {"nodes" (reduce (fn [v no] (conj v (node no))) [{"name" "ME" "group" 1000}] (range 1 101))
   "links" (reduce (fn [v no] (conj v (link no))) [] (range 1 101))})

