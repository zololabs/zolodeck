(ns zolo.gigya.core
  (:use zolodeck.utils.debug
        zolo.utils.http)
  (:require [clj-http.client :as http-client]
            [zolo.setup.config :as config]))

(defn notify-registration [user gigya-user]
  (gigya-oauth-post "socialize.notifyRegistration" {"siteUID" (str (:user/guid user))
                                                    "UID" (:UID gigya-user)})
  user)

(defn delete-account [site-uid]
  (gigya-oauth-post "socialize.deleteAccount" {"UID" site-uid}))

(defn get-gigya-users []
  (gigya-oauth-post "socialize.exportUsers"))

(defn delete-all-accounts []
  (->> (get-gigya-users)
       :users
       (map :UID)
       (map delete-account)
       doall))