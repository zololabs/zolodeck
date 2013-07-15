(ns zolo.storm.utils
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [zolo.domain.user :as user]
            [zolo.utils.logger :as logger]
            [zolo.utils.clojure :as clj]
            [zolo.demonic.core :as demonic]
            [zolo.demonic.helper :as dh]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [zolo.utils.calendar :as zolo-cal])
  (:import [backtype.storm.tuple Values Fields]))

(defn values [& things]
  (Values. (into-array things)))

(defn fields [& things]
  (Fields. things))

(defn short-pause [msg]
  ;;(logger/info "[100 ms pause:]" msg)
  (Thread/sleep 100))

(defn short-log [msg]
  (logger/debug "[100 ms pause:]" msg)
  (Thread/sleep 100))

(defn pause [msg millis]
  (logger/trace "[Sleep ms:" millis "] " msg)
  (Thread/sleep millis))








;; (defn is-recently-permitted?
;;   ([now {refresh-started :user/refresh-started permissions-time :user/fb-permissions-time :as u}]
;;      (and (not refresh-started)
;;           (< (- now (.getTime permissions-time)) (new-user-freshness-period))))
;;   ([u]
;;      (is-brand-new-user? (zolo-cal/now) u)))





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