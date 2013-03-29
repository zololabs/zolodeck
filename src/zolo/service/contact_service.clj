(ns zolo.service.contact-service
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.interaction :as interaction]
            [zolo.store.user-store :as u-store]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.setup.config :as conf]
            [zolo.service.core :as service]
            [zolo.domain.accessors :as dom]))

(defn- fresh-social-identities-for-user-identity [user-identity]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-social-identities provider access-token provider-uid "2012-10-22")))

(defn- fresh-social-identities [u]
  (mapcat fresh-social-identities-for-user-identity (:user/user-identities u)))

(defn- update-contacts [user]
  (->> user
       fresh-social-identities
       (contact/updated-contacts (:user/contacts user))
       (assoc user :user/contacts)))

;; Services
(defn update-contacts-for-user [user-guid]
  (-not-nil-> (u-store/find-by-guid user-guid)
              update-contacts
              u-store/save))

;; (defn update-scores [user-guid]
;;   (when-let [u (u-store/find-by-guid user-guid)]
;;     (let [ibc (-> u dom/inbox-messages-by-contacts interaction/interactions-by-contacts)]
;;       (doeach #(contact/update-score ibc %) (:user/contacts u)))
;;     (u-store/reload u)))