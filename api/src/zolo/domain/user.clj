(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        zolo.utils.debug)
  (:require [zolo.utils.maps :as maps]
            [zolo.facebook.gateway :as fb-gateway]
            [zolo.utils.string :as zolo-str]))

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
  (maps/update-all-map-keys fb-user FB-USER-KEYS))

(defn insert-fb-user [fb-user]
  (-> fb-user
      fb-user->user
      demonic/insert))

(defn find-by-fb-id [fb-id]
  (when fb-id
    (let [entity (-> (demonic/run-query '[:find ?u :in $ ?fb :where [?u :user/fb-id ?fb]]
                                        fb-id)
                     ffirst
                     demonic/load-entity)]
      (when (:db/id entity)
        entity))))

(defn load-from-fb [{:keys [code]}]
  (-> (fb-gateway/code->token code)
      fb-gateway/me))

(defn find-by-fb-signed-request [fb-sr]
  (if-let [zolo-user (find-by-fb-id (:user_id fb-sr))]
    zolo-user
    (-> (load-from-fb fb-sr)
        insert-fb-user)))