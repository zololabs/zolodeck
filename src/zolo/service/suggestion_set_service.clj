(ns zolo.service.suggestion-set-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        conjure.core
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.store.user-store :as u-store]
            [zolo.domain.user-identity :as user-identity]
            [zolo.utils.logger :as logger]
            [zolo.setup.config :as conf]
            [zolo.domain.suggestion-set :as ss]
            [zolo.domain.message :as message]
            [zolo.domain.contact :as contact]
            [zolo.service.core :as service]
            [zolo.store.suggestion-set-store :as ss-store]
            [zolo.utils.calendar :as zolo-cal]))

(defn- create-suggestion-set [u ss-name]
  (it-> (ss/new-suggestion-set u ss-name)
        (ss-store/append-suggestion-set u it)
        (ss/suggestion-set it ss-name)))

(defn- find-or-create-suggestion-set [u]
  (let [ss-name (-> u
                    user/client-date-time
                    ss/suggestion-set-name)]
    (-> (or (ss/suggestion-set u ss-name)
            (create-suggestion-set u ss-name))
        ss/distill)))

(defn find-suggestion-set-for-today [user-guid]
  (-not-nil-> (u-store/find-by-guid user-guid)
              find-or-create-suggestion-set))
