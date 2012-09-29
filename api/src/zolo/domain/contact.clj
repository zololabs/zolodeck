(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolodeck.utils.debug)
  (:require [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.gigya :as gigya-utils]
            [zolo.utils.domain :as utils-domain]
            [zolo.gigya.core :as gigya]
            [zolo.social.core :as social]
            [zolo.domain.social-identity :as social-identity]
            [zolodeck.demonic.core :as demonic]
            [zolo.domain.score :as score]
            [clojure.set :as set]))

;; (defn gigya-contact->basic-contact [gigya-contact social-identities]
;;   {:contact/first-name (social-identity/first-name social-identities)
;;    :contact/last-name (social-identity/last-name social-identities)})

;; (defn gigya-contact->contact [gigya-contact] 
;;   (let [social-identities (-> (gigya-utils/contact-identities gigya-contact)
;;                            social-identity/gigya-user-identities->social-identities)
;;         contact (gigya-contact->basic-contact gigya-contact social-identities)]
;;     (assoc contact :contact/social-identities social-identities)))

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