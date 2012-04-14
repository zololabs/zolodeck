(ns zolo.api.user-api
  (:use zolo.domain.user
        zolo.utils.debug)
  (:require [zolo.incoming.facebook.gateway :as gateway]
            [sandbar.auth :as sandbar]))

(defn upsert-user [request-params]  
  {:user "OK done!"})

(defn friends-list [request-params]
  (gateway/friends-list (:user/fb-auth-token (sandbar/current-user))))

