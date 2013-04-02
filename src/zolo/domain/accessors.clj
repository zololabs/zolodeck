(ns zolo.domain.accessors
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.demonic.core :as demonic]
            [zolo.demonic.helper :as dhelp]
            [zolo.utils.maps :as zolo-maps]
            [zolo.utils.calendar :as zolo-cal]))

;; (defn- contact-identifier [c]
;;   [(:social/provider c) (:social/provider-uid c)])

;; (defn is-provider-id? [m-provider m-from m-to [contact-provider contact-provider-uid]]
;;   ;(println "mp,mf,mt,cp, cpuid:" m-provider "," m-from "," m-to "," contact-provider contact-provider-uid)  
;;   ;(println ">>>" (fent m-provider :db/ident))  
;;   (and (= (-> m-provider dhelp/load-from-db :db/ident) contact-provider)
;;        (or (= m-from contact-provider-uid)
;;            (= m-to contact-provider-uid))))

;; (defn message-belongs? [provider from to legal-provider-infos]
;;   (some #(is-provider-id? provider from to %) legal-provider-infos))

;; (defn- contact-messages [u c]
;;   (let [contact-providers (map contact-identifier (:contact/social-identities c))
;;         qr (demonic/run-query '[:find ?m :in $ ?contact-providers
;;                          :where
;;                          [?m :message/guid]
;;                          [?m :message/provider ?p]
;;                          [?m :message/from ?f]
;;                          [?m :message/to ?t]
;;                          [?m :message/mode "INBOX"]
;;                          [(zolo.domain.accessors/message-belongs? ?p ?f ?t ?contact-providers)]
;;                          ]
;;                        contact-providers)]
;;     (->> qr
;;          (map first)
;;          (map demonic/load-entity))))

;; (defn is-temp-message? [m]
;;   (:temp-message/guid m))

;; (defn is-inbox-message? [m]
;;   (or (= "INBOX" (:message/mode m))
;;       (is-temp-message? m)))

;; (defn is-feed-message? [m]
;;   (or (= "FEED" (:message/mode m))))

;; (defn message-from [m]
;;   (if (is-temp-message? m)
;;     (:temp-message/from m)
;;     (:message/from m)))

;; (defn message-to [m]
;;   (if (is-temp-message? m)
;;     (:temp-message/to m)
;;     (:message/to m)))

;; (defn message-provider [m]
;;   (if (is-temp-message? m)
;;     (:temp-message/provider m)
;;     (:message/provider m)))

;; (defn message-date [m]
;;   (if (is-temp-message? m)
;;     (:temp-message/date m)
;;     (:message/date m)))

;; (defn message-date-in-tz [m tz-offset-minutes]
;;   (-> m
;;       message-date
;;       (zolo-cal/in-time-zone tz-offset-minutes)))

;; (defn message-id [m]
;;   (if (is-temp-message? m)
;;     (:temp-message/guid m)
;;     (:message/message-id m)))

;; (defn message-guid [m]
;;   (if (is-temp-message? m)
;;     (:temp-message/guid m)
;;     (:message/guid m)))

;; (defn interaction-date [i]
;;   (-> i
;;       first
;;       message-date))

;; (defn- update-buckets-for [buckets m contact-ids]
;;   (let [updater (fn [b contact-id]
;;                   (update-in b [[(message-provider m) contact-id]] conj m))]
;;     (reduce updater buckets contact-ids)))

;; (defn- bucket-message [buckets m]
;;   (update-buckets-for buckets m (conj (message-to m) (message-from m))))

;; (defn- bucket-si [c buckets si]
;;   (assoc-in buckets [(contact-identifier si)] c))

;; (defn- bucket-contact [buckets c]
;;   (->> c
;;        :contact/social-identities
;;        (reduce (partial bucket-si c) buckets)))

;; (defn- contacts-by-social-identifier [u]
;;   (->> u
;;        :user/contacts
;;        (reduce bucket-contact {})))

;; (defn messages-by-contacts [u message-filter-fn]
;;   (let [contacts-lookup (contacts-by-social-identifier u)
;;         all-messages (concat (:user/messages u) (:user/temp-messages u))
;;         inbox-messages (filter message-filter-fn all-messages)
;;         mbc (reduce bucket-message {} inbox-messages)]
;;     (reduce #(assoc-in %1 [(contacts-lookup %2)] (sort-by message-date (mbc %2))) {} (keys contacts-lookup))))

;; (defn inbox-messages-by-contacts [u]
;;   (messages-by-contacts u is-inbox-message?))

;; (defn feed-messages-by-contacts [u]
;;   (messages-by-contacts u is-feed-message?))

;; (defn all-messages-by-contacts [u]
;;   (messages-by-contacts u (constantly true)))

;; (defn cleanup-messages [msgs]
;;   (->> msgs
;;        (distinct-by message-id)
;;        (sort-by message-date)))

;; (defn messages-from-imbc [imbc]
;;   (->> imbc
;;        vals
;;        (apply concat)
;;        cleanup-messages))

;; (defn interactions-from-ibc [ibc]
;;   (->> ibc
;;        vals
;;        (apply concat)
;;        (sort-by interaction-date)
;;        squeeze))

;; (defn messages-for-contact [imbc contact]
;;   (->> (imbc contact)
;;        cleanup-messages))

;; (defn inbox-messages-for-user [u]
;;   (->> u
;;        inbox-messages-by-contacts
;;        messages-from-imbc))

;; (defn user-identity-for-provider [u provider]
;;   (->> u
;;        :user/user-identities
;;        (filter #(= provider (:identity/provider %)))
;;        first))