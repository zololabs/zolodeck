(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require
   [zolo.social.core :as social]
   [sandbar.auth :as sandbar]
   [zolo.domain.user :as user]
   [zolo.domain.stats :as stats]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user]
  {:guid (str (:user/guid user))})

(defn provider [request-params]
  (get-in request-params [:provider]))

(defn signin-user [request-params cookies]
  (let [user (or (user/find-by-provider-and-provider-uid (provider request-params) (social/provider-uid request-params cookies))
                 (-> request-params
                     (social/signup-user cookies)
                     user/signup-new-user))]
    (format-user user)))

(defn stats [request-params]
  (let [u (user/fully-loaded-user)]
    {:contacts (stats/contacts-stats u)
     :network (stats/network-stats u)}))


