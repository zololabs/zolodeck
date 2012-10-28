(ns zolo.domain.accessors
  (:use zolodeck.utils.debug)
  (:require [zolodeck.demonic.core :as demonic]
            [zolodeck.demonic.helper :as dhelp]))

;; (defn- message-is-from? [provider-uid m]
;;   (= provider-uid (:message/from m)))

;; (defn- message-is-to? [provider-uid m]
;;   (some #{provider-uid} (:message/to m)))

;; (defn- message-belongs-to-contact [provider provider-uid m]
;;   (and (= provider (:message/provider m))
;;        (or (message-is-from? provider-uid m)
;;            (message-is-to? provider-uid m))))

(defn- contact-identifier [c]
  [(:social/provider c) (:social/provider-uid c)])

;; (defn- messages-for-contact-identifier [[provider provider-uid] messages]
;;   (filter #(message-belongs-to-contact provider provider-uid %) messages))

;; TODO - convert contact-messages into a datalog query
;; (defn contact-messages [u c]
;;   (let [contact-providers (map contact-identifier (:contact/social-identities c))
;;         messages (:user/messages u)]
;;     (mapcat #(messages-for-contact-identifier % messages) contact-providers)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn is-provider-id? [m-provider m-from m-to [contact-provider contact-provider-uid]]
  ;(println "mp,mf,mt,cp, cpuid:" m-provider "," m-from "," m-to "," contact-provider contact-provider-uid)  
  ;(println ">>>" (fent m-provider :db/ident))  
  (and (= (-> m-provider dhelp/load-from-db :db/ident) contact-provider)
       (or (= m-from contact-provider-uid)
           (= m-to contact-provider-uid))))

(defn message-belongs? [provider from to legal-provider-infos]
  (some #(is-provider-id? provider from to %) legal-provider-infos))

(defn contact-messages [u c]
  (let [contact-providers (map contact-identifier (:contact/social-identities c))
        qr (demonic/run-query '[:find ?m :in $ ?contact-providers
                         :where
                         [?m :message/guid]
                         [?m :message/provider ?p]
                         [?m :message/from ?f]
                         [?m :message/to ?t]
                         [?m :message/mode "INBOX"]
                         [(zolo.domain.accessors/message-belongs? ?p ?f ?t ?contact-providers)]
                         ]
                       contact-providers)]
    (->> qr
         (map first)
         (map demonic/load-entity))))

(defn- update-buckets-for [contacts-lookup buckets m contact-ids]
  (let [updater (fn [b contact-id]
                  (let [c (contacts-lookup [(:message/provider m) contact-id])]
                    (if c
                      (update-in b [c] conj m)
                      b)))]
    (reduce updater buckets contact-ids)))

(defn- bucket-message [contacts-lookup buckets m]
  (update-buckets-for contacts-lookup buckets m (conj (:message/to m) (:message/from m))))

(defn- bucket-si [c buckets si]
  (assoc-in buckets [(contact-identifier si)] c))

(defn- bucket-contact [buckets c]
  (->> c
       :contact/social-identities
       (reduce (partial bucket-si c) buckets)))

(defn contacts-by-social-identifier [u]
  (->> u
       :user/contacts
       (reduce bucket-contact {})))

(defn inbox-messages-by-contacts [u]
  (let [contacts-lookup (contacts-by-social-identifier u)
        inbox-messages (filter #(= "INBOX" (:message/mode %)) (:user/messages u))
        mbc (reduce (partial bucket-message contacts-lookup) {} inbox-messages)]
    ;; (print-vals "mbc so far:" mbc)
    ;; (reduce #(do (assoc-in %1 [%2] (or (mbc %2) []))) {} (:user/contacts u))
    mbc
    ))