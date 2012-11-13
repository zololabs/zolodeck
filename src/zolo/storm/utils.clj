(ns zolo.storm.utils
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as conf]
            [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolodeck.utils.clojure :as clj]
            [zolodeck.demonic.core :as demonic]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [zolodeck.utils.calendar :as zolo-cal]))

(def NEW-USER-FRESHNESS-PERIOD (conf/new-user-freshness-millis)) 
(def NEW-USER-WAIT (conf/new-user-freshness-millis))
(def USER-UPDATE-WAIT (conf/user-update-wait-fb-millis))
(def STALE-USERS-WAIT (conf/stale-users-wait-fb-millis))

(defn short-pause [msg millis]
  ;;(logger/info "[1 ms pause:]" msg)
  (Thread/sleep 1))

(defn- pause [msg millis]
  (logger/trace "[Sleep ms:" millis "] " msg)
  (Thread/sleep millis))

(defn refresh-started-recently? [now refresh-started]
  (if refresh-started
    (let [elapsed-since-started (- now (.getTime refresh-started))]
      (< elapsed-since-started STALE-USERS-WAIT))))

(defn last-updated-recently? [now last-updated]
  (if last-updated
    (let [elapsed-since-updated (- now (.getTime last-updated))]
      (< elapsed-since-updated USER-UPDATE-WAIT))))

(defn is-brand-new-user?
  ([now {refresh-started :user/refresh-started :as u}]
     (and (not refresh-started)
          (< (- now (.getTime (user/creation-time u))) NEW-USER-FRESHNESS-PERIOD)))
  ([u]
     (is-brand-new-user? (zolo-cal/now) u)))

(defn recently-created-or-updated [{guid :user/guid
                                    last-updated :user/last-updated
                                    refresh-started :user/refresh-started :as u}]
  (let [now (zolo-cal/now)
        recent? (or (is-brand-new-user? now u)
                    (refresh-started-recently? now refresh-started)
                    (last-updated-recently? now last-updated))]
    (logger/trace "User:" guid ", recently updated:" recent?)
    recent?))

(defn new-user-in-tx-report [tx-report]
  (demonic/in-demarcation
   (->> tx-report
        :tx-data
        (demonic/run-raw-query '[:find ?ug :in ?us $data
                                 :where 
                                 [$data _ ?us ?ug _ true]] (demonic/schema-attrib-id :user/guid))
        ffirst
        str)))

(defn inst-seconds-ago [seconds]
  (zolo-cal/millis->instant (.getMillis (time/minus (time/now) (time/secs seconds)))))

(defn setup-dummies []
  (future
    (pause "Delaying for 5 seconds..." 5000)
    (let [dummies [
                   {:user/first-name "AB" :user/guid (clj/random-guid) :user/last-updated (inst-seconds-ago 600)}
                   {:user/first-name "CD" :user/guid (clj/random-guid) :user/last-updated (inst-seconds-ago 600)}
                   {:user/first-name "EF" :user/guid (clj/random-guid) :user/last-updated (inst-seconds-ago 600)}
                   {:user/first-name "GH" :user/guid (clj/random-guid) :user/last-updated (inst-seconds-ago 600)}
                   {:user/first-name "IJ" :user/guid (clj/random-guid) :user/last-updated (inst-seconds-ago 600) :user/refresh-started (inst-seconds-ago 5)}
                   {:user/first-name "KL" :user/guid (clj/random-guid) :user/last-updated (inst-seconds-ago 180)}
                   {:user/first-name "MN" :user/guid (clj/random-guid)}]]
      (logger/info "Creating dummies now!")
      (demonic/in-demarcation
       (doseq [d dummies]
         (demonic/insert d))))))