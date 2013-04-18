(ns zolo.api.server-api
  (:use zolo.utils.debug
        zolo.web.status-codes)
  (:require
   [zolo.store.user-store :as u-store]
   [zolo.utils.calendar :as zcal]
   [zolo.demonic.core :as demonic]
   [zolo.utils.logger :as logger]))

;;TODO Test this namespace

(def SERVER_GUID #uuid "97a34244-61fe-4a05-8a9e-95013349e298")

(defn- new-server-obj []
  (-> {:server/guid SERVER_GUID :server/last-updated (zcal/now-instant)}
      demonic/insert-and-reload))

(defn- find-server-object []
  (-> (demonic/run-query '[:find ?u :in $ ?guid :where [?u :server/guid ?guid]] SERVER_GUID)
      ffirst
      demonic/load-entity))

(defn- find-or-create-server-object []
  (let [obj (find-server-object)]
    (if obj
      obj
      (new-server-obj))))

(defn- within-minutes? [old-inst new-inst number-of-minutes]
  (if-not (and old-inst new-inst)
    false
    (<= (zcal/minutes-between old-inst new-inst) number-of-minutes)))

(defn- update-server-obj [server-obj updated-time]
  (-> server-obj
      (assoc :server/last-updated updated-time)
      demonic/insert))

(defn check-datomic []
  (let [now (zcal/now-instant)
        server-obj (find-or-create-server-object)
        last-updated (:server/last-updated server-obj)]
    (update-server-obj server-obj now)
    (within-minutes? last-updated now 7)))

(defn status [request-params]
  (if (check-datomic)
    {:status (STATUS-CODES :ok)
     :body {:no-of-users (u-store/count-users)}}
    (throw (RuntimeException. "Didn't write to Datomic in past 7 minutes..."))))


