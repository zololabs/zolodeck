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

(defn signin-user [request-params cookies]
  (let [user (or (print-vals "EXIST:" (user/find-by-login-provider-uid (social/provider-uid request-params cookies)))
                 (print-vals "NEW:" (-> request-params
                                        (social/signup-user cookies)
                                        user/signup-new-user)))]
    (format-user user)))

(defn stats [request-params]
  (let [u (user/fully-loaded-user)]
    {:contacts (stats/contacts-stats u)
     :network (stats/network-stats u)}))


