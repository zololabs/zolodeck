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
            [zolo.domain.social-detail :as social-detail]
            [zolodeck.demonic.core :as demonic]
            [zolo.domain.score :as score]
            [clojure.set :as set]))

(defn gigya-contact->basic-contact [gigya-contact social-details]
  {:contact/first-name (social-detail/first-name social-details)
   :contact/last-name (social-detail/last-name social-details)})

(defn gigya-contact->contact [gigya-contact] 
  (let [social-details (-> (gigya-utils/contact-identities gigya-contact)
                           social-detail/gigya-user-identities->social-details)
        contact (gigya-contact->basic-contact gigya-contact social-details)]
    (assoc contact :contact/social-details social-details)))

(defn contact-lookup-table [c]
  (->> (:contact/social-details c)
      (map (fn [s] {(social-detail/social-detail-info s) c}))
      (apply merge)))

(defn contacts-lookup-table [contacts]
  (or (->> (map contact-lookup-table contacts)
           (apply merge))
      {}))

(defn find-contact-from-lookup [user social-details]
  (let [contacts-lookup (contacts-lookup-table (:user/contacts user))]
    (->> social-details
         (map social-detail/social-detail-info)
         (some #(contacts-lookup %)))))

(defn find-contact-by-provider-info [user provider-info]
  ((contacts-lookup-table (:user/contacts user)) provider-info))

(defn create-contact [user provider-info]
  ;;(demonic/append-single user :user/contacts contact)
  ;;TODO Need to create a social detail for new contact 
  )

(defn fresh-contacts [u]
  (map gigya-contact->contact (gigya/get-friends-info u)))

(defn update-contact [user fresh-contact]
  (let [contact (->> (:contact/social-details fresh-contact)
                     (find-contact-from-lookup user))]
    (if contact
      (assoc contact :contact/social-details
             (utils-domain/update-fresh-entities-with-db-id
               (:contact/social-details contact)
               (:contact/social-details fresh-contact)
               social-detail/social-detail-info
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
  (let [reloaded-c (reload c)]
    (demonic/append-single reloaded-c :contact/score (score/create reloaded-c))))

(defn score [c]
  (or (:score/value (:contact/score c))
      -1))