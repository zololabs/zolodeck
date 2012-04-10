(ns zolo.api.user-api
  (:use zolo.domain.user
        zolo.utils.debug)
  (:require [zolo.ext.facebook :as facebook]
            [sandbar.auth :as sandbar]))

(defn upsert-user [request-params]
  {:user "OK Need to Implement"})

(defn friends-list [request-params]
  (facebook/friends-list (:access-token (sandbar/current-user))))

