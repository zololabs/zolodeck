(ns zolo.service.distiller.user
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.user-identity :as ui]
            [zolo.domain.user :as u]
            [zolo.utils.logger :as logger]))

(defn distill [user]
  (when user 
    {:user/guid (str (:user/guid user))
     :user/emails (ui/email-ids user)
     :user/data-ready-in (u/data-ready-in-remaining-seconds user)
     :user/updated (not (nil? (:user/last-updated user)))
     ;;TODO test
     :user/all-permissions-granted (u/all-permissions-granted? user)}))