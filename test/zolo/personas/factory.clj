(ns zolo.personas.factory
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.demonic.test
        conjure.core)
  (:require [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.marconi.context-io.fake-api :as fake-email]
            [zolo.gateway.pento.core :as pento]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.social.facebook.messages :as fb-messages]
            [zolo.social.facebook.stream :as fb-stream]
            [zolo.social.email.gateway :as email-gateway]
            [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.store.user-store :as u-store]
            [zolo.service.user-service :as u-service]
            [zolo.domain.message :as message]
            [zolo.utils.calendar :as zcal]
            [zolo.utils.maps :as zmaps]))

(defn fb-request-params
  ([fb-user permission-granted?]
     (fb-request-params fb-user permission-granted? 420))
  ([fb-user permission-granted? login-tz]
     (let [fb-creds (fb-lab/login-creds fb-user)]
       {:login_provider "FACEBOOK"
        :guid nil
        :login_tz login-tz
        :permissions_granted permission-granted?
        :access_token (get-in fb-creds [:providerLoginInfo :authResponse :accessToken])
        :login_provider_uid (get-in fb-creds [:providerLoginInfo :authResponse :userID])})))

(defn email-request-params
  ([email-user]
     (email-request-params email-user true))
  ([email-user permission-granted?]
     (email-request-params email-user permission-granted? 420))
  ([email-user permission-granted? login-tz]
     {:login_provider "EMAIL"
      :guid nil
      :login_tz login-tz
      :permissions_granted permission-granted?
      :access_token (:account-id email-user)
      :login_provider_uid (:account-id email-user)}))

(defn fake-extended-user-info [at uid]
  (-> uid
      fb-lab/get-user
      fb-lab/extended-user-info))

(defn fake-friends-list [at uid]
  (-> uid
      fb-lab/get-user
      fb-lab/fetch-friends))


;; TODO - should find user by AT, instead of current-user, esp when there are more than 1 FB accounts per user
(defn fake-fetch-inbox [at date]
  (let [res (-> (fb-lab/current-user)
                (fb-lab/fetch-messages date))]
    ;(print-vals "FakeFetchInbox returning " (count res) " messages...")
    res))

(defn fake-extended-access-token [& args]
  (random-str))

(defn fake-fetch-feed [access-token contact-id yyyy-MM-dd-string]
 (fb-lab/fetch-feeds (fb-lab/get-user contact-id)))

(defn fake-email-account [account-id]
  (fake-email/fetch-account account-id))

(defn fake-fetch-email-contacts [account-id date-in-seconds]
  (fake-email/fetch-contacts account-id))

(defn fake-fetch-email-messages [account-id date-in-seconds]
  (fake-email/fetch-messages account-id))

(defn fake-pento-score-all [email-address-info-list]
  (let [defaults (zipmap (map :email email-address-info-list) (repeat 100))
        results (merge defaults {"admin@thoughtworks.com" -10.23
                                 "donotreply@Man.com" -20.46})]
    (zmaps/transform-vals-with results #(float %2))))

(defmacro in-social-lab [& body]
  `(marconi/in-lab
    (stubbing [fb-gateway/extended-user-info fake-extended-user-info
               fb-gateway/friends-list fake-friends-list
               fb-messages/fetch-inbox fake-fetch-inbox
               fb-stream/recent-activity fake-fetch-feed
               fb-gateway/extended-access-token fake-extended-access-token
               email-gateway/get-account fake-email-account
               email-gateway/get-contacts fake-fetch-email-contacts 
               email-gateway/get-messages fake-fetch-email-messages
               pento/score-all fake-pento-score-all]
      ~@body)))

(defmacro in-email-lab [& body]
  `(marconi/in-lab
    (stubbing [email-gateway/get-account fake-email-account
               email-gateway/get-contacts fake-fetch-email-contacts 
               email-gateway/get-messages fake-fetch-email-messages
               pento/score-all fake-pento-score-all]
      ~@body)))

(defmacro in-fb-lab [& body]
  `(marconi/in-lab
    (stubbing [fb-gateway/extended-user-info fake-extended-user-info
               fb-gateway/friends-list fake-friends-list
               fb-messages/fetch-inbox fake-fetch-inbox
               fb-stream/recent-activity fake-fetch-feed
               fb-gateway/extended-access-token fake-extended-access-token
               pento/score-all fake-pento-score-all]
      ~@body)))

(defn fetch-fb-ui [fb-user]
  (it-> fb-user
        (fb-request-params it true)
        (social/fetch-user-identity it)))

(defn create-domain-user-from-fb-user [fb-user]
  (it-> fb-user
        (fetch-fb-ui it)
        (assoc {} :user/user-identities [it])
        (assoc it :user/login-tz 0)
        (assoc it :user/data-ready-in (zcal/now-instant))))

(defn create-db-user-from-fb-user [fb-user]
  (-> fb-user
      create-domain-user-from-fb-user
      u-store/save))

(defn fetch-email-ui [email-user]
  (-> email-user
      email-request-params
      social/fetch-user-identity))

(defn create-domain-user-from-email-user [email-user]
  (it-> email-user
        (fetch-email-ui it)
        (assoc {} :user/user-identities [it])
        (assoc it :user/login-tz 0)
        (assoc it :user/data-ready-in (zcal/now-instant))))

(defn create-db-user-from-email-user [email-user]
  (-> email-user
      create-domain-user-from-email-user
      u-store/save))

(defn create-temp-message [u to-user-provider-id text]
  (message/create-temp-message (user/provider-id u :provider/facebook)
                               to-user-provider-id
                               :provider/facebook
                               "thread-id"
                               "subject"
                               text))

;; (defn domain-persona [f]
;;   (with-demonic-demarcation true (f)))

(defmacro domain-persona [& body]
  `(with-demonic-demarcation true ~@body))

(defmacro in-test-demarcation [& body]
  `(with-demonic-demarcation true ~@body))