(ns zolo.domain.accessors
  (:require [zolodeck.demonic.core :as demonic]))

(defn- message-is-from? [provider-uid m]
  (= provider-uid (:message/from m)))

(defn- message-is-to? [provider-uid m]
  (some #{provider-uid} (:message/to m)))

(defn- message-belongs-to-contact [provider provider-uid m]
  (and (= provider (:message/provider m))
       (or (message-is-from? provider-uid m)
           (message-is-to? provider-uid m))))

(defn- contact-identifier [c]
  [(:social/provider c) (:social/provider-uid c)])

(defn- messages-for-contact-identifier [[provider provider-uid] messages]
  (filter #(message-belongs-to-contact provider provider-uid %) messages))

;; TODO - convert contact-messages into a datalog query
(defn contact-messages [u c]
  (let [contact-providers (map contact-identifier (:contact/social-identities c))
        messages (:user/messages u)]
    (mapcat #(messages-for-contact-identifier % messages) contact-providers)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- message-belongs? [provider from to legal-provider-infos]
  (let [is-provider-id? (fn [m-provider m-from m-to [contact-provider contact-provider-uid]]
                          (and (= m-provider contact-provider)
                               (or (= m-from contact-provider-uid)
                                   (some (fn [to] (= contact-provider-uid to)) m-to))))]
    (some is-provider-id? legal-provider-infos)))

(defn contact-messages-datalog [u c]
  (let [contact-providers (map contact-identifier (:contact/social-identities c))]
    (demonic/run-query '[:find ?m :in $ ?contact-providers
                         :where
                         [?m :message/guid]
                         [?p :message/provider]
                         [?f :message/from]
                         [?t :message/to]
                         [(:message-belongs-to-contact? ?p ?f ?t ?contact-providers)]] contact-providers)))