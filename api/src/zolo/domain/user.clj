(ns zolo.domain.user
  (:use zolo.setup.datomic-setup        
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        zolodeck.utils.debug)
  (:require [zolo.facebook.gateway :as fb-gateway]
            [zolo.facebook.inbox :as fb-inbox]
            [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [clojure.set :as set]))

(def FB-USER-KEYS 
     {:first_name :user/first-name
      :last_name :user/last-name
      :gender :user/gender
      :link :user/fb-link
      :username :user/fb-username
      :email :user/fb-email
      :id :user/fb-id
      :auth-token :user/fb-auth-token})

(defn fb-user->user [fb-user]
  (zolo-maps/update-all-map-keys fb-user FB-USER-KEYS))

(defn insert-fb-user [fb-user]
  (-> fb-user
      fb-user->user
      demonic/insert))

(defn find-by-fb-id [fb-id]
  (if fb-id
    (-> (demonic/run-query '[:find ?u :in $ ?fb :where [?u :user/fb-id ?fb]] fb-id)
        ffirst
        demonic/load-entity)))

(defn reload [u]
  (find-by-fb-id (:user/fb-id u)))

(defn load-from-fb [{:keys [code]}]
  (-> code
      fb-gateway/code->token
      fb-gateway/me))

(defn update-facebook-friends [fb-id]
  (let [user (find-by-fb-id fb-id)]
    (->> user
         :user/fb-auth-token
         fb-gateway/friends-list
         (map contact/fb-friend->contact)
         (contact/update-contacts user)
         demonic/insert)))

(defn update-facebook-inbox [fb-id]
  (let [user (find-by-fb-id fb-id)]
    (->>  user
          :user/fb-auth-token
          fb-inbox/fetch-inbox
          (map message/fb-message->message)
          (message/merge-messages user)
          (map demonic/insert)
          doall)))

(defn find-by-fb-signed-request [fb-sr]
  (if-let [zolo-user (find-by-fb-id (:user_id fb-sr))]
    zolo-user
    (-> fb-sr
        load-from-fb
        insert-fb-user)))

(defn update-scores [u]
  (doall (map contact/update-score (:user/contacts u)))
  (reload u))