(ns zolo.domain.accessors)

(defn- message-is-from [provider-uid m]
  (= provider-uid (:message/from m)))

(defn- message-is-to [provider-uid m]
  (some #{provider-uid} (:message/to m)))

(defn- message-belongs-to-contact [provider-id provider-uid m]
  (and (= provider-id (:message/provider m))
       (or (message-is-from provider-uid m)
           (message-is-to provider-uid m))))

(defn- contact-identifier [c]
  [(:social/provider c) (:social/provider-uid c)])

(defn- messages-for-contact-identifier [[provider provider-uid] messages]
  (filter #(message-belongs-to-contact provider provider-uid %) messages))

;; TODO - convert contact-messages into a datalog query
(defn contact-messages [u c]
  (let [contact-providers (map contact-identifier (:contact/social-identities c))
        messages (:user/messages u)]
    (mapcat #(messages-for-contact-identifier % messages) contact-providers)))