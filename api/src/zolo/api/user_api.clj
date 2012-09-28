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
;; (defn format-user [user request-params]
;;   (-> {:guid (str (:user/guid user))}
;;       (gigya/add-gigya-uid-info request-params)))

(defn signup-user [request-params]
  (-> request-params
      (print-vals-> "SIGNUP:")
      social/login-user
      ;(gigya-core/notify-registration request-params)
      ;(format-user request-params)
      ))

(defn stats [request-params]
  (let [u (user/fully-loaded-user)]
    {:contacts (stats/contacts-stats u)
     :network (stats/network-stats u)}))


