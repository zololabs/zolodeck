(ns zolo.service.suggestion-set-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.core :as d-core]
            [zolo.store.user-store :as u-store]
            [zolo.domain.user-identity :as user-identity]
            [zolo.utils.logger :as logger]
            [zolo.setup.config :as conf]
            [zolo.domain.suggestion-set :as ss]
            [zolo.domain.message :as message]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.contact :as contact]
            [zolo.service.core :as service]
            [zolo.store.suggestion-set-store :as ss-store]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.domain.suggestion-set.strategy.random :as ss-s-random]))

(defn- suggestion-set-name [u]
  (-> u
      user/client-date-time
      ss/suggestion-set-name))

(defn- create-suggestion-set [u ss-name]
  (it-> (ss/new-suggestion-set u ss-name ss-s-random/compute)
        (ss-store/append-suggestion-set u it)
        (ss/suggestion-set it ss-name)))

(defn- find-or-create-suggestion-set [u]
  (let [ibc (interaction/ibc u (contact/person-contacts u))
        ss-name (suggestion-set-name u)]
    (-> (or (ss/suggestion-set u ss-name)
            (create-suggestion-set u ss-name))
        (ss/distill ibc))))

(defn find-suggestion-set-for-today [user-guid]
  (if-let [u (u-store/find-by-guid user-guid)]
    (d-core/run-in-tz-offset (:user/login-tz u)
                             (find-or-create-suggestion-set u))))
