(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolodeck.utils.debug)
  (:require [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
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

(defn find-contact-by-provider-info [user provider-info]
  ((contacts-lookup-table (:user/contacts user)) provider-info))

(defn create-contact [user provider-info]
  ;;(demonic/append-single user :user/contacts contact)
  ;;TODO Need to create a social detail for new contact 
  )

(defn recent-message-time [c]
  (->> c
       :contact/messages
       (sort-by :message/date)
       last
       :message/date))

(defn no-messages? [c]
  (-> c :contact/messages empty?))

(defn forgotten-contacts [u ever-messaged? number]
  (let [contacts (->> u :user/contacts (sort-by recent-message-time))
        contacts (if ever-messaged?
                   (remove no-messages? contacts)
                   (filter no-messages? contacts))]
    (take number contacts)))

(defn weak-contacts [u number]
  (->> u
       :user/contacts
       (sort-by :contact/score)
       (take number)))

(defn strong-contacts [u number]
  (->> u
       :user/contacts
       (sort-by :contact/score)
       reverse
       (take number)))

(defn fresh-contacts-for-social-identity [social-identity]
  (let [{provider :social/provider
         access-token :social/auth-token
         provider-uid :social/provider-uid} social-identity]
    (social/fetch-contacts provider access-token provider-uid)))

(defn fresh-contacts [u]
  (mapcat fresh-contacts-for-social-identity (:user/social-identities u)))

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

(defn update-score [c]
  (->  (assoc c :contact/score (score/calculate c))
       demonic/insert))