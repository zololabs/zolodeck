(ns zolo.service.storm-service
  (:use zolo.utils.debug
        zolo.utils.clojure
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.domain.message :as m]
            [zolo.store.user-store :as u-store]
            [zolo.domain.user-identity :as user-identity]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.utils.logger :as logger]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.setup.config :as conf]
            [zolo.service.core :as service]
            [zolo.demonic.core :as demonic]
            [zolo.demonic.helper :as dh]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.utils.calendar :as zcal]))

(defn refresh-started-recently? [now refresh-started]
  (if refresh-started
    (let [elapsed-since-started (- now (.getTime refresh-started))]
      (< elapsed-since-started 300000))))

(defn last-updated-recently? [now last-updated]
  (if last-updated
    (let [elapsed-since-updated (- now (.getTime last-updated))]
      (< elapsed-since-updated (conf/user-update-wait-fb-millis)))))

(defn is-brand-new-user?
  ([now {refresh-started :user/refresh-started creation-time :user-temp/creation-time :as u}]
     (and (not refresh-started)
          (< (- now (.getTime creation-time)) (conf/new-user-freshness-millis))))
  ([u]
     (is-brand-new-user? (zcal/now) u)))

(defn recently-created-or-updated [{guid :user/guid
                                    last-updated :user/last-updated
                                    refresh-started :user/refresh-started :as u}]
  (let [now (zcal/now)
        recent? (or (is-brand-new-user? now u)
                    (refresh-started-recently? now refresh-started)
                    (last-updated-recently? now last-updated))]
    ;; (logger/trace "User:" guid ", recently updated:" recent?)
    ;; (print-vals "guid:" guid
    ;;               "brand-new:" (is-brand-new-user? now u)
    ;;               "ref-recent:" (refresh-started-recently? now refresh-started)
    ;;               "last-updated:" (last-updated-recently? now last-updated))
    recent?))

(defn refresh-guids-to-process []
  ;;(logger/info "Finding Refresh GUIDS to process...")
  (demonic/in-demarcation
   (->> (u-store/find-all-users-for-refreshes)
        (remove recently-created-or-updated)
        (domap #(str (:user/guid %))))))

(defn permissions-granted-in-tx-report [tx-report]
  (demonic/in-demarcation
   (->> tx-report
        :tx-data
        (demonic/run-raw-query '[:find ?ue :in ?us $data
                                 :where 
                                 [$data ?ue ?us true _ true]] (demonic/schema-attrib-id :identity/permissions-granted))
        ffirst
        dh/load-from-db
        :user/_user-identities
        :user/guid
        str)))

(defn new-user-in-tx-report [tx-report]
  (demonic/in-demarcation
   (->> tx-report
        :tx-data
        (demonic/run-raw-query '[:find ?ug :in ?us $data
                                 :where 
                                 [$data _ ?us ?ug _ true]] (demonic/schema-attrib-id :user/guid))
        ffirst
        str)))