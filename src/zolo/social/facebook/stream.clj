(ns zolo.social.facebook.stream
  (:use zolodeck.utils.debug
        zolodeck.utils.string)
  (:require [zolo.social.facebook.gateway :as gateway]
            [clj-time.coerce :as ctc]
            [zolodeck.utils.calendar :as zolo-cal]))

(def STREAM-TYPES
  {80 "Link Posted"}
  )

(defn stream [auth-token provider-uid since-yyyy-mm-dd-string]
  (gateway/run-fql auth-token
                   (str
;;                     " SELECT post_id, actor_id, target_id, message
;; FROM stream
;; WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed')
;; AND is_hidden = 0"

;; "SELECT post_id, actor_id, created_time, tagged_ids, message, source_id, type
;; FROM stream
;; WHERE
;; filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me())
;; AND is_hidden = 0"

"SELECT post_id, actor_id, created_time, target_id, message, source_id, type FROM stream WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed') AND is_hidden = 0 AND created_time > " (zolo-cal/to-seconds since-yyyy-mm-dd-string))))

(defn stream-filters [auth-token provider-uid]
  (gateway/run-fql auth-token (str
                               "SELECT filter_key, name, type FROM stream_filter WHERE uid=" provider-uid)))

(defn stream-tags [auth-token provider-uid]
  (gateway/run-fql auth-token
                   (str "SELECT post_id,actor_id FROM stream_tag WHERE target_id = " provider-uid)))

(defn recent-activity-until [auth-token provider-uid since-yyyy-mm-dd-string]
  (gateway/get-json-pages-until
     (gateway/recent-activity-url provider-uid)
     auth-token
     {:fields "from,created_time,message,story,to,type,picture,link,icon" :limit 200}
     (fn [i]
       ;(print-vals "Item date:" (ctc/to-date-time (:created_time i)) "test:" (.isBefore (ctc/to-date-time (:created_time i)) (ctc/to-date-time until-yyyy-mm-dd-string)))
       (.isAfter (ctc/to-date-time since-yyyy-mm-dd-string) (ctc/to-date-time (:created_time i))))))

