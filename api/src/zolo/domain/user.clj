(ns zolo.domain.user
  (:use zolo.setup.datomic-setup        
        [slingshot.slingshot :only [throw+ try+]]
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        zolodeck.utils.debug)
  (:require [zolo.facebook.gateway :as fb-gateway]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.utils.domain :as domain]
            [zolo.utils.gigya :as gigya-utils]
            [zolo.utils.readers :as readers]
            [zolo.gigya.core :as gigya]
            [zolo.domain.social-detail :as social-detail]
            [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [sandbar.auth :as sandbar]
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

(defn current-user []
  (dissoc (sandbar/current-user) :username :roles))

(defn fb-user->user [fb-user]
  (zolo-maps/update-all-map-keys fb-user FB-USER-KEYS))

(defn find-by-guid [guid]
  (when guid
    (-> (demonic/run-query '[:find ?u :in $ ?guid :where [?u :user/guid ?guid]] guid)
        ffirst
        demonic/load-entity)))

(defn find-by-guid-string [guid]
  (find-by-guid (java.util.UUID/fromString guid)))

(defn find-by-login-provider-uid [login-provider-uid]
  (when login-provider-uid
    (-> (demonic/run-query '[:find ?u :in $ ?login-provider-uid :where [?u :user/login-provider-uid ?login-provider-uid]] login-provider-uid)
        ffirst
        demonic/load-entity)))

(defn reload-using-login-provider-uid [u]
  (find-by-login-provider-uid (:user/login-provider-uid u)))

(defn gigya-user->basic-user [gigya-user social-details]
  {:user/first-name (social-detail/first-name social-details)
   :user/last-name (social-detail/last-name social-details)
   :user/login-provider-uid (:loginProviderUID gigya-user)})

(defn gigya-user->user [gigya-user] 
  (let [social-details (-> (gigya-utils/identities gigya-user)
                           social-detail/gigya-user-identities->social-details)
        user (gigya-user->basic-user gigya-user social-details)]
    (assoc user :user/social-details social-details)))

(defn signup-new-user [gigya-user]
  (-> gigya-user
      gigya-user->user
      demonic/insert
      reload-using-login-provider-uid))

(defn update-contacts [u]
  (->> u
      gigya/get-friends-info
      (map contact/gigya-contact->contact)
      (contact/update-contacts u)
      demonic/insert
      ))

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

(defn update-scores [u]
  (doall (map contact/update-score (:user/contacts u)))
  (reload u))