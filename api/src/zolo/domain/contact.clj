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

(def FB-CONTACT-KEYS
    {:first_name :contact/first-name
     :last_name :contact/last-name
     :gender :contact/gender

     :id :contact/fb-id
     :link :contact/fb-link
     :birthday :contact/fb-birthday
     :picture :contact/fb-picture-link})

(defn format-birthday [fb-friend]
  ;;TODO Junk implementation need to design
  (assoc fb-friend :birthday
         (cond
          (nil? (:birthday fb-friend))  (zolo-cal/date-string->instant "MM/dd/yyyy" "01/01/1900")
          (= 5 (count (:birthday fb-friend))) (zolo-cal/date-string->instant "MM/dd/yyyy"
                                                                             (str (:birthday fb-friend) "/1900"))
          :else (zolo-cal/date-string->instant "MM/dd/yyyy" (:birthday fb-friend)))))

(defn fb-friend->contact [fb-friend]
  (-> fb-friend
      format-birthday
      (zolo-maps/update-all-map-keys FB-CONTACT-KEYS)))

(defn find-by-user-and-contact-fb-id [user contact-fb-id]
  (first (filter #(= contact-fb-id (:contact/fb-id %)) (:user/contacts user))))

(defn find-by-social-details [social-details]
  nil)

;; (defn create-contact [user contact-a-map]
;;   (demonic/append-single user :user/contacts contact-a-map)
;;   (find-by-user-and-contact-fb-id user (:contact/fb-id contact-a-map)))

(defn gigya-contact->basic-contact [gigya-contact social-details]
  {:contact/first-name (social-detail/first-name social-details)
   :contact/last-name (social-detail/last-name social-details)})

(defn gigya-contact->contact [gigya-contact] 
  (let [social-details (-> (gigya-utils/contact-identities gigya-contact)
                           social-detail/gigya-user-identities->social-details)
        contact (gigya-contact->basic-contact gigya-contact social-details)]
    (assoc contact :contact/social-details social-details)))

(defn find-contact-from-lookup [contacts-lookup social-details]
  (->> social-details
       (map social-detail/social-detail-info)
       (some #(contacts-lookup %))))

(defn contact-lookup-table [c]
  (->> (:contact/social-details c)
      (map (fn [s] {(social-detail/social-detail-info s) c}))
      (apply merge)))

(defn contacts-lookup-table [contacts]
  (->> (map contact-lookup-table contacts)
       (apply merge)))

(defn create-contact [user contact]
  (demonic/append-single user :user/contacts contact))

(defn fresh-contacts [u]
  (map gigya-contact->contact (gigya/get-friends-info u)))

(defn update-contact [user contacts-lookup fresh-contact]
  (let [contact (->> (:contact/social-details fresh-contact)
                     (find-contact-from-lookup contacts-lookup))]
    (if contact
      (assoc contact :contact/social-details
             (utils-domain/update-fresh-entities-with-db-id
               (:contact/social-details contact)
               (:contact/social-details fresh-contact)
               social-detail/social-detail-info))
      fresh-contact)))

(defn update-contacts [user]
  (let [fresh-cs (fresh-contacts user)
        contacts-lookup (contacts-lookup-table fresh-cs)
        updated-contacts (map #(update-contact user contacts-lookup %) fresh-cs)]
    (-> (assoc user :user/contacts
               (utils-domain/update-fresh-entities-with-db-id (:user/contacts user) updated-contacts :contact/guid))
        demonic/insert)))

(defn update-score [c]
  (demonic/append-single c :contact/scores (score/create c)))


