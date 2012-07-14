(ns zolo.domain.zolo-graph
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.utils.fe :as fe]            
            [zolo.domain.message :as message]
            [zolo.domain.score :as score]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.math :as zolo-math]))

(defn user-guid [zg]
  (first (keys zg)))

(defn user-fb-id [zg]
  (get-in zg [(user-guid zg) :about  :facebook :id]))

(defn contact-guid [zg-contact]
  (first (keys zg-contact)))

(defn contact-fb-id [zg-contact]
  (get-in zg-contact [(contact-guid zg-contact) :about :facebook :id]))

(defn contact-guids [zg]
  (keys (get-in zg [(user-guid zg) :contacts])))

(defn contacts [zg]
  (get-in zg [(user-guid zg) :contacts]))

(defn contact [zg c-id]
  (get-in zg [(user-guid zg) :contacts c-id]))

(defn upsert-contact [zg c]
  (update-in zg [(user-guid zg) :contacts] #(merge % c)))

(defn messages [zg c-id]
  (get-in zg [(user-guid zg) :contacts c-id :messages]))

(defn all-messages [zg]
  (reduce (fn [acc c-id]
            (concat acc (messages zg c-id)))
          []
          (contact-guids zg)))

(defn add-message [zg c-id m]
  (update-in zg
             [(user-guid zg) :contacts c-id :messages]
             #(conj % m)))

(defn scores [zg c-id]
  (get-in zg [(user-guid zg) :contacts c-id :scores]))

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
          (contact-guids zg)))

(defn add-score [zg c-id s]
  (update-in zg
             [(user-guid zg) :contacts c-id :scores]
             #(conj % s)))

(defn contacts-with-score-between [zg lower upper]
  (filter #(and (>= (score-value zg (:guid %)) lower)
                (< (score-value zg (:guid %)) upper))
          (vals (contacts zg))))

(defn contacts-stats [zg]
  {:total (count (contacts zg))
   :strong (count (contacts-with-score-between zg 250 10000000))
   :medium (count (contacts-with-score-between zg 50 250))
   :weak (count (contacts-with-score-between zg 0 50))})

(defn network-stats [zg]
  {:average (zolo-math/average (map #(score-value zg %) (contact-guids zg)))
   ;;TODO This needs to be tested
   :weak-contacts (doall (map fe/format-contact (take 5 (sort-by #(score-value zg (:guid %)) (vals (contacts zg))))))})

;; Construction
(defn score->zg-score [s]
  (zolo-maps/update-all-map-keys s score/ZG-SCORE-KEYS))

(defn scores->zg-scores [scores]
  (map score->zg-score scores))

(defn message->zg-message [msg]
  (zolo-maps/update-all-map-keys msg message/ZG-MESSAGE-KEYS))

(defn messages->zg-messages [messages]
  (map message->zg-message messages))

(defn contact->zolo-contact [c]
  (when c
    {:guid (c :contact/guid)
     :about 
     {:first-name (c :contact/first-name)
      :last-name (c :contact/last-name)
      :gender (c :contact/gender)
      :facebook {:id (c :contact/fb-id)
                 :link (c :contact/fb-link)
                 :birthday (c :contact/fb-birthday)
                 :picture (c :contact/fb-picture-link)}}
     :messages (messages->zg-messages (:contact/messages c))
     :scores (scores->zg-scores (:contact/scores c))}))

(defn contacts->zg-contacts [contacts]
  (->> contacts
       (map contact->zolo-contact)
       (mapcat (fn [zc] [(:guid zc) zc]))
       (apply hash-map)))

(defn user->zolo-graph [user]
  (when user
    {(:user/guid user)
     {:guid (:user/guid user)
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