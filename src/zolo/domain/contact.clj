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

;; (defn find-contact-by-provider-info [user provider-info]
;;   ((contacts-lookup-table (:user/contacts user)) provider-info))

;; (defn create-contact [user provider-info]
;;   ;;(demonic/append-single user :user/contacts contact)
;;   ;;TODO Need to create a social detail for new contact 
;;   )

(defn fresh-contacts-for-user-identity [user-identity]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-contacts provider access-token provider-uid)))

(defn fresh-contacts [u]
  (mapcat fresh-contacts-for-user-identity (:user/user-identities u)))

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

;;TODO Duplication find-by-guid
(defn find-by-guid [guid]
  (when guid
    (-> (demonic/run-query '[:find ?c :in $ ?guid :where [?c :contact/guid ?guid]] guid)
        ffirst
        demonic/load-entity)))

(defn reload [c]
  (find-by-guid (:contact/guid c)))

(defn update-score [u c]
  (->  (assoc c :contact/score (score/calculate u c))
       demonic/insert))

