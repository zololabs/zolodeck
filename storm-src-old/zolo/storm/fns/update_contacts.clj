(ns zolo.storm.fns.update-contacts
  (:use zolo.utils.debug
        zolo.storm.utils)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]))

(gen-class
 :name zolo.storm.fns.UpdateContacts
 :extends storm.trident.operation.BaseFunction)

(defn -execute [this tuple collector]
  (let [user-guid (.getStringByField tuple "user-guid")
        user (user/find-by-guid-string user-guid)]
    (when user
      (logger/trace "updating contacts for user id:" user-guid)
      (contact/update-contacts)
      (doseq [c (:user/contacts (user/reload user))]
        (.emit collector (values (str (:contact/guid c))))))))