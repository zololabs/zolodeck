(ns zolo.service.contact-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolo.domain.social-identity :as si]            
            [zolo.domain.interaction :as interaction]
            [zolo.store.user-store :as u-store]
            [zolo.store.contact-store :as c-store]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.gateway.pento.core :as pento]
            [zolo.setup.config :as conf]
            [zolo.service.core :as service]
            [zolo.domain.core :as d-core]
            [zolo.utils.maps :as zmaps]))

(defn set-person-score [si]
  (if-not (si/is-email? si)
    si
    (assoc si :social/email-person-score (-> si :social/provider-uid pento/score))))

(defn- fresh-social-identities-for-user-identity [user-identity]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-social-identities provider access-token provider-uid "2000-10-22")))

(defn- fresh-social-identities [u]
  (->> (:user/user-identities u)
       (mapcat fresh-social-identities-for-user-identity)
       (map set-person-score)))

(defn- update-contacts [user]
  (->> user
       fresh-social-identities
       (contact/updated-contacts (:user/contacts user))
       (assoc user :user/contacts)))

;;TODO Move to core
(defn request-params->contact-attrs [params]
  (zmaps/transform-keys-with params {:muted :contact/muted
                                     :person :contact/is-a-person}))

(def val-request
  {:muted [:optional :boolean]
   :person [:optional :boolean]})

;; Services
(defn update-contacts-for-user [u]
  (-not-nil-> u
              update-contacts
              u-store/save))

(defn update-scores [u]
  (when u
    (let [ibc (-> u
                  message/messages-by-contacts
                  interaction/interactions-by-contacts)]
      (doeach #(contact/update-score ibc %) (:user/contacts u))
      (u-store/reload u))))

(defn get-contact-by-guid [u guid]
  (d-core/run-in-tz-offset (:user/login-tz u)
                           (if-let [ibc (interaction/ibc u)]
                             (-> (c-store/find-by-guid guid)
                                 (contact/distill ibc)))))

(defn update-contact [u c params]
  (when (and u c)
    (d-core/run-in-tz-offset (:user/login-tz u)
                             (let [updated-c (it-> (select-keys params [:muted :person])
                                                   (service/validate-request! it val-request)
                                                   (request-params->contact-attrs it)
                                                   (merge c it)
                                                   (c-store/save it))
                                   u (u-store/reload u)
                                   ibc (interaction/ibc u)]
                               (contact/distill updated-c ibc))))) 