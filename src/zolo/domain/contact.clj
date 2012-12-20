(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolodeck.utils.debug)
  (:require [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.logger :as logger]
            [zolo.utils.domain :as utils-domain]
            [zolo.social.core :as social]
            [zolo.domain.social-identity :as social-identity]
            [zolo.domain.accessors :as dom]
            [zolodeck.demonic.core :as demonic]
            [zolo.domain.score :as score]
            [clojure.set :as set]))

(defn contact-lookup-table [c]
  (->> (:contact/social-identities c)
      (map (fn [s] {(social-identity/social-identity-info s) c}))
      (apply merge)))

(defn contacts-lookup-table [contacts]
  (or (->> (map contact-lookup-table contacts)
           (apply merge))
      {}))

(defn find-contact-from-lookup [user social-identities]
  (let [contacts-lookup (contacts-lookup-table (:user/contacts user))]
    (->> social-identities
         (map social-identity/social-identity-info)
         (some #(contacts-lookup %)))))

(defn provider-info-by-provider [u]
  (->> u
       :user/contacts
       (mapcat :contact/social-identities)
       (map #(select-keys % [:social/provider :social/provider-uid]))
       (group-by :social/provider)))

(defn fresh-contacts-for-user-identity [user-identity]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-contacts provider access-token provider-uid "2012-10-22")))

;;TODO Duplication find-by-guid
(defn find-by-guid [guid]
  (when guid
    (-> (demonic/run-query '[:find ?c :in $ ?guid :where [?c :contact/guid ?guid]] guid)
        ffirst
        demonic/load-entity)))

(defn find-by-guid-string [guid-string]
  (when guid-string
    (find-by-guid (java.util.UUID/fromString guid-string))))

(defn reload [c]
  (find-by-guid (:contact/guid c)))

(defn fresh-contacts [u]
  (mapcat fresh-contacts-for-user-identity (:user/user-identities u)))

(defn suggested-contacts [u suggestion-set]
  (filter #(= suggestion-set (:contact/suggestion-set %)) (:user/contacts u)))

(defn suggest-contact [suggestion-set c]
  (if (= suggestion-set (:contact/suggestion-set c))
    c
    (-> c
        (assoc :contact/suggestion-set suggestion-set)
        demonic/insert
        reload)))

(defn update-contact [user fresh-contact]
  (let [contact (->> (:contact/social-identities fresh-contact)
                     (find-contact-from-lookup user))]
    (if contact
      (assoc contact :contact/social-identities
             (utils-domain/update-fresh-entities-with-db-id
               (:contact/social-identities contact)
               (:contact/social-identities fresh-contact)
               social-identity/social-identity-info
               :social/guid))
      fresh-contact)))

(defn update-contacts [user]
  (let [fresh-cs (fresh-contacts user)
        updated-contacts (map #(update-contact user  %) fresh-cs)]
    (-> (assoc user :user/contacts updated-contacts)
        demonic/insert)))


(defn update-score [ibc c]
  (-> (assoc c :contact/score (score/calculate ibc c))
      demonic/insert))

(defn last-send-message [ibc c]
  (->> (ibc c)
       dom/messages-from-interactions
       (sort-by dom/message-date)
       last))

(defn is-contacted-on? [ibc c dt]
  (print-vals "Client Date : " dt)
  (zolo-cal/same-day-instance? dt (dom/message-date (print-vals "Last Send Message Date :" (last-send-message ibc c)))))

