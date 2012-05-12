(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolodeck.utils.debug)
  (:require [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [clojure.set :as set]))

(defn is-contact-same? [existing-contact fresh-contact]
  (= (select-keys existing-contact (keys fresh-contact)) fresh-contact))

(defn find-updated-contact-fb-ids [existing-contacts-grouped fresh-contacts-grouped]
  (remove (fn [k] 
            (is-contact-same? (existing-contacts-grouped k) 
                              (fresh-contacts-grouped k))) 
          (keys existing-contacts-grouped)))

(defn find-updated-contacts [existing-contacts-grouped fresh-contacts-grouped]
  (map (fn [k] 
         (merge (existing-contacts-grouped k) (fresh-contacts-grouped k))) 
       (find-updated-contact-fb-ids existing-contacts-grouped fresh-contacts-grouped)))

(defn group-by-fb-id [contacts]
  (-> (group-by :contact/fb-id contacts)
      (zolo-maps/transform-vals-with (fn [_ v] (first v)))))

(defn find-added-contacts [existing-contacts-grouped fresh-contacts-grouped]
  (map fresh-contacts-grouped (set/difference (set (keys fresh-contacts-grouped)) 
                                              (set (keys existing-contacts-grouped)))))

(defn merge-contacts [user fresh-contacts]
  (let [existing-contacts-grouped (group-by-fb-id (:user/contacts user))
        fresh-contacts-grouped (group-by-fb-id fresh-contacts)
        adds (find-added-contacts existing-contacts-grouped fresh-contacts-grouped)
        updates (find-updated-contacts existing-contacts-grouped fresh-contacts-grouped)]
    (assoc user :user/contacts (concat adds updates))))

