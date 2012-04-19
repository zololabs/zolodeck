(ns zolo.domain.user
  (:use zolo.setup.datomic-setup
        [zolodeck.demonic.core :only [insert run-query load-entity] :as demonic]
        zolo.utils.domain
        zolo.utils.debug)
  (:require [zolo.facebook.gateway :as fb-gateway]
            [zolo.utils.string :as zolo-str]))

(defn insert-fb-user [fb-user]
  (-> fb-user
      fb-user->user
      (demonic/insert-and-transform-with convert-to-regular-map)))

(defn find-by-fb-id [fb-id]
  (if fb-id
    (-> (demonic/run-query '[:find ?u :in $ ?fb :where [?u :user/fb-id ?fb]] fb-id)
        ffirst
        (demonic/load-and-transform-with convert-to-regular-map))))

(defn load-from-fb [{:keys [code]}]
  (-> code
      fb-gateway/code->token
      fb-gateway/me))

(defn find-by-fb-signed-request [fb-sr]
  (if-let [zolo-user (find-by-fb-id (:user_id fb-sr))]
    zolo-user
    (-> fb-sr
        load-from-fb
        insert-fb-user)))