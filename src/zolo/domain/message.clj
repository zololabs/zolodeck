(ns zolo.domain.message
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [clojure.set :as set]
            [zolo.utils.maps :as zolo-maps]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolo.domain.user-identity :as ui]
            [zolo.social.core :as social]            
            [zolo.demonic.schema :as schema]
            [zolo.demonic.core :as demonic]
            [zolo.utils.logger :as logger]))

(def MESSAGES-START-TIME #inst "2000-10-22")
(def MESSAGES-START-TIME-SECONDS (-> MESSAGES-START-TIME .getTime zolo-cal/to-seconds))

;;TODO test
(defn is-temp-message? [m]
  (:temp-message/guid m))

;;TODO test
(defn message-date
  ([m]
     (if (is-temp-message? m)
       (:temp-message/date m)
       (:message/date m)))
  ([m tz-offset-minutes]
     (-not-nil-> (message-date m)
                 (zolo-cal/in-time-zone tz-offset-minutes))))

;;TODO test
(defn get-last-message-date [u]
  (or (->> u
           :user/messages
           (remove is-temp-message?)
           (sort-by :message/date)
           last
           :message/date)
      MESSAGES-START-TIME))


;;TODO test
(defn message-provider [m]
  (if (is-temp-message? m)
    (:temp-message/provider m)
    (:message/provider m)))

(defn message-from [m]
  (if (is-temp-message? m)
    (:temp-message/from m)
    (:message/from m)))

(defn message-to [m]
  (if (is-temp-message? m)
    (:temp-message/to m)
    (:message/to m)))

(defn- update-buckets-for [buckets m contact-ids]
  (let [updater (fn [b contact-id]
                  (update-in b [[(message-provider m) contact-id]] conj m))]
    (reduce updater buckets contact-ids)))

(defn- bucket-message [buckets m]
  (update-buckets-for buckets m (conj (message-to m) (message-from m))))

;;TODO This method is in a wrong place
(defn- contact-identifier [c]
  [(:social/provider c) (:social/provider-uid c)])

(defn- bucket-si [c buckets si]
  (assoc-in buckets [(contact-identifier si)] c))

(defn- bucket-contact [buckets c]
  (->> c
       :contact/social-identities
       (reduce (partial bucket-si c) buckets)))

(defn- contacts-by-social-identifier [u]
  (->> u
       :user/contacts
       (reduce bucket-contact {})))

(defn messages-by-contacts [u message-filter-fn]
  (let [contacts-lookup (contacts-by-social-identifier u)
        all-messages (concat (:user/messages u) (:user/temp-messages u))
        inbox-messages (filter message-filter-fn all-messages)
        mbc (reduce bucket-message {} inbox-messages)]
    (reduce #(assoc-in %1 [(contacts-lookup %2)] (sort-by message-date (mbc %2))) {} (keys contacts-lookup))))

;;TODO test
(defn- is-sent-to [c msg]
  (some #(not (nil? %))
        (domap #((message-to msg) (:social/provider-uid %)) (:contact/social-identities c))))

;;TODO Dont need this anymore as there will be only one type of message?!?
(defn is-inbox-message? [m]
  (or (= "INBOX" (:message/mode m))
      (is-temp-message? m)))

(defn inbox-messages-by-contacts [u]
  (messages-by-contacts u is-inbox-message?))

;;TODO test
(defn last-sent-message [c msgs]
  (->> msgs
       (filter #(is-sent-to c %))
       (sort-by message-date)
       last))

;; (defn feeds-start-time-seconds []
;;   (-> (zolo-cal/now-joda)
;;       (zolo-cal/minus 1 :week)
;;       (zolo-cal/to-seconds)))

;; (defn message-identifier [m]
;;   [(dom/message-provider m) (dom/message-id m)])

;; (defn refreshed-messages [user fresh-messages]
;;   (utils-domain/update-fresh-entities-with-db-id (:user/messages user)
;;                                                  fresh-messages
;;                                                  message-identifier
;;                                                  dom/message-guid))

;; (defn get-messages-for-user-identity [user-identity last-updated-seconds]
;;   (let [{provider :identity/provider
;;          access-token :identity/auth-token
;;          provider-uid :identity/provider-uid} user-identity]
;;     (social/fetch-messages provider access-token provider-uid last-updated-seconds)))

;; (defn get-messages-for-user [user]
;;   (let [date (->> user
;;                   dom/inbox-messages-for-user
;;                   (remove dom/is-temp-message?)
;;                   (sort-by :message/date)
;;                   last
;;                   :message/date)
;;         seconds (if date (-> date .getTime zolo-cal/to-seconds))]
;;     (->> user
;;          :user/user-identities
;;          (mapcat #(get-messages-for-user-identity % (or seconds MESSAGES-START-TIME-SECONDS))))))

;; (defn delete-temp-messages [user]
;;   (->> user
;;        :user/temp-messages
;;        (doeach demonic/delete)))

;; (defn update-inbox-messages [user]
;;   (->> user
;;        get-messages-for-user
;;        (demonic/append-multiple user :user/messages))
;;   (delete-temp-messages user))

;; (defn- update-messages-for-contact-and-provider [user feed-messages si]
;;   (try-catch
;;    (let [{contact-uid :social/provider-uid provider :social/provider} si
;;          auth-token (-> user (dom/user-identity-for-provider provider) :identity/auth-token)
;;          fmg (group-by dom/message-provider feed-messages)
;;          date (->> provider fmg (sort-by dom/message-date) last dom/message-date)
;;          seconds (if date (-> date .getTime zolo-cal/to-seconds))
;;          feed-messages (social/fetch-feed provider auth-token contact-uid (or seconds (feeds-start-time-seconds)))]
;;      (demonic/append-multiple user :user/messages feed-messages))))

;; (defn update-messages-for-contact [user contact]
;;   (let [fmbc (-> user dom/feed-messages-by-contacts)
;;         feed-messages (fmbc contact)
;;         identities (:contact/social-identities contact)]
;;     (doeach #(update-messages-for-contact-and-provider user feed-messages %) identities)))

;; (defn update-feed-messages-for-all-contacts [user]
;;   (->> user
;;        :user/contacts
;;        (pdoeach #(update-messages-for-contact user %) 20 true)))

;;TODO test
(defn create-temp-message [from-uid to-uid provider thread-id text]
  {:temp-message/provider provider
   :temp-message/from from-uid
   :temp-message/to to-uid
   :temp-message/text text
;  :temp-message/thread-id thread-id
   :temp-message/mode "INBOX"
   :temp-message/date (zolo-cal/now-instant)})