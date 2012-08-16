(ns zolo.domain.user
  (:use zolo.setup.datomic-setup        
        [slingshot.slingshot :only [throw+ try+]]
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        zolodeck.utils.debug)
  (:require [zolo.facebook.gateway :as fb-gateway]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.utils.domain :as domain]
            [zolo.utils.gigya :as gigya]
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

(def GIGYA-IDENTITY-KEYS
  {:age :social/age
   :country :social/country
   :gender :social/gender
   :lastName :social/last-name
   :state :social/state
   :photoURL :social/photo-url
   :birthDay :social/birth-day
   :thumbnailURL :social/thumbnail-url
   :firstName :social/first-name
   :city :social/city
   :birthMonth :social/birth-month
   :nickname :social/nickname 
   :birthYear :social/birth-year
   :email :social/email 
   :profileURL :social/profile-url
   :providerUID :social/provider-uid
   :zip :social/zip
})

(defn fb-user->user [fb-user]
  (zolo-maps/update-all-map-keys fb-user FB-USER-KEYS))

(defn gigya-user->basic-user [gig])

(defn gigya-fb-user->user [gigya-user]
  (let [social (-> gigya-user
                   gigya/facebook-identity
                   (zolo-maps/update-all-map-keys GIGYA-IDENTITY-KEYS)
                   (dissoc :social/gender :social/provider)                   
                   domain/force-schema-types)
        user {:user/first-name (:social/first-name social)
              :user/last-name (:social/last-name social)}]
    (assoc user :user/social-details [social])))

(defn gigya-user->user [gigya-user]
  (cond
   (gigya/is-facebook-login? gigya-user) (gigya-fb-user->user gigya-user)
   :else (throw+ {:type :bad-request
                  :message "Invalid login provider specified with signup request"})))

(defn signup-new-user [gigya-user]
  (-> gigya-user
      gigya-user->user
      demonic/insert))

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
          (fb-inbox/fetch-inbox)
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