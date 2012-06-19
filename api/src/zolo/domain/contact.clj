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

;;TODO No test present for this namespace :(

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

(defn is-contact-same? [existing-contact fresh-contact]
  (= (select-keys existing-contact (keys fresh-contact)) fresh-contact))

(defn find-updated-contact-fb-ids [existing-contacts-grouped fresh-contacts-grouped]
  (remove (fn [k] 
            (is-contact-same? (existing-contacts-grouped k) 
                              (fresh-contacts-grouped k))) 
          (keys existing-contacts-grouped)))

(defn find-by-user-and-fb-id [user contact-fb-id]
;; [:find ?c_name ?r_name
;;  :where
;;  [?c :community/name ?c_name]
;;  [?c :community/neighborhood ?n]
;;  [?n :neighborhood/district ?d]
;;  [?d :district/region ?r]
;;  [?r :db/ident ?r_name]]

  (print-vals (filter #(= contact-fb-id (:contact/fb-id %)) (:user/contacts user)))
  ;; (-> (demonic/run-query '[:find ?c
  ;;                          :in $ ?user-guid ?fb-id
  ;;                          :where
  ;;                          [?u :user/guid ?user-guid]
  ;;                          [?u :user/contacts ?c]
  ;;                          [?c :contact/fb-id ?fb-id]] (:guid user) contact-fb-id)
  ;;     ffirst
  ;;     demonic/load-entity)

  ;; (-> (demonic/run-query '[:find ?c :in $ ?fb :where [?c :contact/fb-id ?fb]] contact-fb-id)
  ;;     ffirst
  ;;     demonic/load-entity)
  )

(defn group-by-fb-id [contacts]
  (utils-domain/group-by-attrib contacts :contact/fb-id))

(defn update-fresh-contacts-with-db-id [existing-contacts fresh-contacts]
  (let [existing-contacts-grouped (group-by-fb-id existing-contacts)
        fresh-contacts-grouped (group-by-fb-id fresh-contacts)]
    (map
     (fn [[fb-id fresh-contact]]
       (assoc fresh-contact :db/id (:db/id (existing-contacts-grouped fb-id))))
     fresh-contacts-grouped)))

(defn update-contacts [user fresh-contacts]
  (let [existing (:user/contacts user)
        updated-contacts (if (empty? existing)
                           fresh-contacts
                           (update-fresh-contacts-with-db-id existing fresh-contacts))]
    (assoc user :user/contacts updated-contacts)))

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



