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

(defn create-contact [user contact-a-map]
  (demonic/append-single user :user/contacts contact-a-map)
  (find-by-user-and-contact-fb-id user (:contact/fb-id contact-a-map)))

(defn gigya-contact->basic-contact [gigya-contact social-details]
  {:contact/first-name (social-detail/first-name social-details)
   :contact/last-name (social-detail/last-name social-details)
   :contact/gigya-uid (:UID gigya-contact)})

(defn gigya-contact->contact [gigya-contact] 
  (let [social-details (-> (gigya-utils/contact-identities gigya-contact)
                           social-detail/gigya-user-identities->social-details)
        contact (gigya-contact->basic-contact gigya-contact social-details)]
    (assoc contact :contact/social-details social-details)))

(defn update-contacts [user fresh-contacts]
  (assoc user :user/contacts
         (utils-domain/update-fresh-entities-with-db-id (:user/contacts user) fresh-contacts :contact/gigya-uid)))

(defn update-score [c]
  (demonic/append-single c :contact/scores (score/create c)))





