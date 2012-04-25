(ns zolo.utils.domain
  (:require [zolodeck.utils.maps :as maps]))

(def FB-USER-KEYS 
     {:first_name :user/first-name
      :last_name :user/last-name
      :gender :user/gender
      :link :user/fb-link
      :username :user/fb-username
      :email :user/fb-email
      :id :user/fb-id
      :auth-token :user/fb-auth-token})

(defn datomic-key->regular-key [datomic-key]
  (-> datomic-key
      name
      (.replace "fb-" "")
      keyword))

(defn convert-to-regular-map [user-entity]
  (if user-entity
    (-> user-entity
        (select-keys (vals FB-USER-KEYS))
        (maps/update-all-map-keys datomic-key->regular-key)
        (merge {:db/id (:db/id user-entity)}))))

(defn fb-user->user [fb-user]
  (maps/update-all-map-keys fb-user FB-USER-KEYS))