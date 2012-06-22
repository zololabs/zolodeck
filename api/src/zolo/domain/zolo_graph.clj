(ns zolo.domain.zolo-graph
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolodeck.utils.maps :as zolo-maps]))

(defn user-zolo-id [zg]
  (first (keys zg)))

;;TODO Need to add test
(defn user-fb-id [zg]
  (get-in zg [(user-zolo-id zg) :about  :facebook :id]))

(defn contact-zolo-id [zg-contact]
  (first (keys zg-contact)))

;;TODO Need to add test
(defn contact-fb-id [zg-contact]
  (get-in zg-contact [(contact-zolo-id zg-contact) :about :facebook :id]))

(defn contact-zolo-ids [zg]
  (keys (get-in zg [(user-zolo-id zg) :contacts])))

(defn contacts [zg]
  (get-in zg [(user-zolo-id zg) :contacts]))

(defn contact [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id]))

(defn upsert-contact [zg c]
  (update-in zg [(user-zolo-id zg) :contacts] #(merge % c)))

(defn messages [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id :messages]))

(defn all-messages [zg]
  (reduce (fn [acc c-id]
            (concat acc (messages zg c-id)))
          []
          (contact-zolo-ids zg)))

(defn add-message [zg c-id m]
  (update-in zg
             [(user-zolo-id zg) :contacts c-id :messages]
             #(conj % m)))

(defn scores [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id :scores]))

(defn score [zg c-id]
  (last (sort-by :at (scores zg c-id))))

(defn has-score? [zg c-id]
  (not (nil? (score zg c-id))))

(defn score-value 
  ([s]
     (if (nil? s)
       -1
       (:value s)))
  ([zg c-id]
     (score-value (score zg c-id))))

(defn all-scores [zg]
  (reduce (fn [acc c-id]
            (concat acc (scores zg c-id)))
          []
          (contact-zolo-ids zg)))

(defn add-score [zg c-id s]
  (update-in zg
             [(user-zolo-id zg) :contacts c-id :scores]
             #(conj % s)))


;; Construction

(defn message->zg-message [msg]
  (zolo-maps/update-all-map-keys msg message/ZG-MESSAGE-KEYS))

(defn messages->zg-messages [messages]
  (map message->zg-message messages))

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
   :messages (messages->zg-messages (:contact/messages c))
   :scores []})

(defn contacts->zg-contacts [contacts]
  (->> contacts
       (map contact->zolo-contact)
       (mapcat (fn [zc] [(:zolo-id zc) zc]))
       (apply hash-map)))

(defn user->zolo-graph [user]
  (when user
    {(:user/guid user)
     {:zolo-id (:user/guid user)
      :about 
      {:first-name (:user/first-name user)
       :last-name (:user/last-name user)
       :gender (:user/gender user)
       :facebook {:link (:user/fb-link user)
                  :username (:user/fb-username user)
                  :email (:user/fb-email user)
                  :id (:user/fb-id user)
                  :auth-token (:user/fb-auth-token user)}}
      :contacts (contacts->zg-contacts (:user/contacts user))}}))