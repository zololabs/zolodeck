(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolodeck.utils.debug)
  (:require [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolodeck.demonic.core :as demonic]
            [clojure.set :as set]))

(def FB-CONTACT-KEYS
    {:first_name :contact/first-name
     :last_name :contact/last-name
     :gender :contact/gender

     :id :contact/fb-id
     :link :contact/fb-link
     :birthday :contact/fb-birthday
     :picture :contact/fb-picture-link})

(defn fb-friend->contact [fb-friend]
  (-> fb-friend
      (assoc :birthday (zolo-cal/date-string->instant "MM/dd/yyyy" (:birthday fb-friend)))
      (zolo-maps/update-all-map-keys FB-CONTACT-KEYS)))

(defn create-contact [user contact-a-map]
  (->> contact-a-map
       (conj (map #(dissoc % :contact/messages) (:user/contacts user)))
       (demonic/append user :user/contacts)))

(defn find-by-user-and-contact-fb-id [user contact-fb-id]
  (first (filter #(= contact-fb-id (:contact/fb-id %)) (:user/contacts user))))


(defn update-contacts [user fresh-contacts]
  (assoc user :user/contacts
         (utils-domain/update-fresh-entities-with-db-id (:user/contacts user) fresh-contacts :contact/fb-id)))

;; Zolo Graph Related Stuff
(defn contact->zolo-contact [c]
  {:zolo-id (c :contact/guid)
   :about 
   {:first-name (c :contact/first-name)
    :last-name (c :contact/last-name)
    :gender (c :contact/gender)
    :facebook {:id (c :contact/fb-id)
               :link (c :contact/fb-link)
               :birthday (c :contact/fb-birthday)
               :picture (c :contact/fb-picture-link)}}
   :messages []
   :scores []})

(defn contacts->zg-contacts [contacts]
  (->> contacts
       (map contact->zolo-contact)
       (mapcat (fn [zc] [(:zolo-id zc) zc]))
       (apply hash-map)))





