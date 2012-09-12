(ns zolo.api.user-api
  (:use zolo.domain.user
        zolodeck.utils.debug)
  (:require [zolo.facebook.gateway :as gateway]
            [zolo.gigya.core :as gigya-core]
            [zolo.utils.gigya :as gigya]
            [sandbar.auth :as sandbar]
            [zolo.domain.user :as user]
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

;;TODO Junk function. Need to design the app
(defn fully-loaded-user
  ([u]
     (user/update-contacts u)
     (print-vals "contacts done")
     (user/update-messages u)
     (print-vals "messages done")
     (user/update-scores (user/reload u))
     (print-vals "scores done")     
     (user/reload u))
  ([]
     (fully-loaded-user (user/current-user))))

(defn stats [request-params]
  (let [u (fully-loaded-user)
        ;;zg (zg/user->zolo-graph u)
        ]
    {:contacts nil;;(zg/contacts-stats zg)
     :network nil ;;(zg/network-stats zg)
     }
    ))


