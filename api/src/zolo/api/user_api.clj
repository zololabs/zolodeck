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
  ([user]
     (let [u-fb-id (:user/fb-id user)]
        (if (= 0 (count (:user/contacts user)))
         (do
           (user/update-facebook-friends u-fb-id)
           (user/update-facebook-inbox u-fb-id)
           (user/update-scores (user/reload user))
           (user/reload user))
         user)
         ))
  ([]
     (fully-loaded-user (sandbar/current-user))))

(defn stats [request-params]
  (let [u (fully-loaded-user)
        zg (zg/user->zolo-graph u)]
    {:contacts (zg/contacts-stats zg)
     :network (zg/network-stats zg)}))


