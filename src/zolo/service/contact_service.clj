(ns zolo.service.contact-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolo.domain.thread :as t]
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
            [zolo.utils.maps :as zmaps]
            [zolo.service.distiller.contact :as c-distiller]
            [zolo.service.distiller.thread :as t-distiller]))

(def REPLY-TO "reply_to")
(def FOLLOW-UP "follow_up")

(defn select-pento-keys [si]
  (let [key-names {:social/provider-uid :email
                   :social/nickname :name
                   :social/sent-count :sent_count
                   :social/received-count :received_count}]
    (-> si
        (select-keys (keys key-names))
        (zmaps/update-all-map-keys key-names))))

(defn set-person-score [si score-lookup]
  (if-not (si/is-email? si)
    si
    (assoc si :social/email-person-score (score-lookup (:social/provider-uid si)))))

(defn set-person-scores [si-list]
  (let [scores (->> si-list
                    (map select-pento-keys)
                    pento/score-all)]
    (map #(set-person-score % scores) si-list)))

(defn- fresh-social-identities-for-user-identity [user-identity]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-social-identities provider access-token provider-uid "2000-10-22")))

(defn- fresh-social-identities [u]
  (->> (:user/user-identities u)
       (mapcat fresh-social-identities-for-user-identity)
       set-person-scores))

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
    (let [ibc (interaction/ibc u (:user/contacts u))]
      (doeach #(contact/update-score ibc %) (:user/contacts u))
      (u-store/reload u))))

(defn get-contact-by-guid [u guid]
  (d-core/run-in-tz-offset (:user/login-tz u)
                           (if-let [ibc (interaction/ibc u (:user/contacts u))]
                             (-> (c-store/find-entity-by-guid guid)
                                 (c-distiller/distill u ibc)))))

(defn update-contact [u c params]
  (when (and u c)
    (d-core/run-in-tz-offset (:user/login-tz u)
                             (let [updated-c (it-> (select-keys params [:muted :person])
                                                   (service/validate-request! it val-request)
                                                   (request-params->contact-attrs it)
                                                   (merge c it)
                                                   (c-store/save it))
                                   u (u-store/reload u)
                                   ibc (interaction/ibc u (:user/contacts u))]
                               (c-distiller/distill updated-c u ibc)))))


(defn contacts-for-reply-to [u thread-limit thread-offset]
  (it-> u
        (t/find-reply-to-threads it thread-limit thread-offset)
        (t-distiller/distill-by-contacts u it)
        (map (fn [[c threads]] (merge c {:reply-to-threads threads})) it)))

(defn contacts-for-follow-up [u thread-limit thread-offset]
  (it-> u
        (t/find-follow-up-threads it thread-limit thread-offset)
        (t-distiller/distill-by-contacts u it)
        (map (fn [[c threads]] (merge c {:follow-up-threads threads})) it)))

(defn selector-contacts [u selector thread-limit thread-offset]
  (condp = selector
    REPLY-TO (contacts-for-reply-to u thread-limit thread-offset)
    FOLLOW-UP (contacts-for-follow-up u thread-limit thread-offset)
    (throw+ {:type :bad-request :message (str "Unknown Contacts selector : " selector)})))

(defn apply-selectors [selectors thread-limit thread-offset user]
  (if (empty? selectors)
    (:user/contacts user)
    (->> selectors
         distinct
         (map #(selector-contacts user % thread-limit thread-offset))
         (apply concat))))

(defn list-contacts [user options]
  (d-core/run-in-tz-offset (:user/login-tz user)
                           (let [{:keys [selectors thread_limit thread_offset limit offset]} options]
                             (->> user
                                  (apply-selectors selectors thread_limit thread_offset)
                                  (apply-pagination limit offset)))))
