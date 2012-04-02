(ns zolo.domain.user
  (:use [zolo.infra.datomic :only [upsert run-query load-entity] :as datomic]))

(defn insert-new-user [first-name last-name facebook-id fb-auth-token]
  (datomic/upsert {:user/first-name first-name
                   :user/last-name last-name
                   :user/facebook-id facebook-id
                   :user/fb-auth-token fb-auth-token}))

(defn find-by-facebook-id [facebook-id]
  (-> (datomic/run-query '[:find ?u :in $ ?fb :where [?u :user/facebook-id ?fb]]
                         facebook-id)
      ffirst
      datomic/load-entity))