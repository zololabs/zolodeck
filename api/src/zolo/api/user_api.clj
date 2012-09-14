(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require [zolo.facebook.gateway :as gateway]
            [zolo.gigya.core :as gigya-core]
            [zolo.utils.gigya :as gigya]
            [sandbar.auth :as sandbar]
            [zolo.domain.user :as user]
            [zolo.domain.stats :as stats]
            [zolo.domain.zolo-graph :as zg]
            [zolo.viz.d3 :as d3]))

;;TODO (siva) this is an experiment ..need to change this though
(defn format-user [user request-params]
  (-> {:guid (str (:user/guid user))}
      (gigya/add-gigya-uid-info request-params)))

(defn signup-user [request-params]
  (-> request-params
      user/signup-new-user
      (gigya-core/notify-registration request-params)
      (format-user request-params)))

(defn stats [request-params]
  (let [u (user/fully-loaded-user)]
    {:contacts (stats/contacts-stats u)
     :network (stats/network-stats u)}))


