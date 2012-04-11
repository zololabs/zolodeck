(ns zolo.api.user-api
  (:use zolo.domain.user
        zolo.utils.debug)
  (:require ]))
  (:require [zolo.incoming.facebook.gateway :as gateway]
            [sandbar.auth :as sandbar]))

(defn upsert-user [request-params]
  {:user "OK Need to Implement"})

(defn friends-list [request-params]
  (gateway/friends-list (:access-token (sandbar/current-user))))

