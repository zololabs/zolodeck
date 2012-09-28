(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require
   ;; [zolo.gigya.core :as gigya-core]
   ;; [zolo.utils.gigya :as gigya]
   [zolo.social.core :as social]
   [sandbar.auth :as sandbar]
   [zolo.domain.user :as user]
   [zolo.domain.stats :as stats]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user]
  {:guid (str (:user/guid user))})

(defn signup-user [request-params]
  (-> request-params
      social/login-user
      user/signup-new-user
      format-user
      ))

(defn stats [request-params]
  (let [u (user/fully-loaded-user)]
    {:contacts (stats/contacts-stats u)
     :network (stats/network-stats u)}))


