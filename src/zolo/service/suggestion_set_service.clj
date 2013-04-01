(ns zolo.service.suggestion-set-service
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.store.user-store :as u-store]
            [zolo.domain.user-identity :as user-identity]
            [zolo.utils.logger :as logger]
            [zolo.setup.config :as conf]
            [zolo.domain.suggestion-set :as ss]
            [zolo.service.core :as service]
            [zolodeck.utils.calendar :as zolo-cal]))

(defn- suggestion-set [cs]
  {:name (ss/suggestion-set-name (zolo-cal/now-instant))
   :contacts cs})

(defn find-suggestion-set-for-today [user-guid]
  (-not-nil-> (u-store/find-by-guid user-guid)
              :user/contacts
              suggestion-set)
  ;; (-not-nil-> (u-store/find-by-guid user-id)
  ;;             interaction/ibc
  ;;             (suggestion-set/find-first-by-client-date)
  ;;             (suggestion-set/format ibc))
)