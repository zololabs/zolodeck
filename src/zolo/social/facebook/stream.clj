(ns zolo.social.facebook.stream
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        zolodeck.utils.string)
  (:require [zolo.social.facebook.gateway :as gateway]
            [clj-time.coerce :as ctc]
            [zolodeck.utils.calendar :as zolo-cal]
            [clj-http.client :as http]
            [clojure.data.json :as json]))

(def STREAM-TYPES
  {80 "Link Posted"}
  )

(defn stream [auth-token provider-uid since-yyyy-mm-dd-string]
  (gateway/run-fql auth-token
     (str "SELECT post_id, actor_id, created_time, target_id, message, source_id, type FROM stream WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed') AND is_hidden = 0 AND created_time > " (zolo-cal/to-seconds since-yyyy-mm-dd-string))))

(defn stream-filters [auth-token provider-uid]
  (gateway/run-fql auth-token (str "SELECT filter_key, name, type FROM stream_filter WHERE uid=" provider-uid)))

(defn stream-tags [auth-token provider-uid]
  (gateway/run-fql auth-token (str "SELECT post_id,actor_id FROM stream_tag WHERE target_id = " provider-uid)))

(defn recent-activity [auth-token provider-uid since-unix-time-stamp]
  (:data
   (gateway/get-json
    (gateway/recent-activity-url provider-uid)
    auth-token
    {:fields "from,created_time,message,story,to,type,picture,link,icon"
     :since since-unix-time-stamp
     :limit 200}
    ;;#(.isAfter (zolo-cal/seconds->joda-time since-unix-time-stamp) (ctc/to-date-time (:created_time %)))
    )))

(defn recent-activity-url [provider-uid access-token]
  (gateway/create-url (str provider-uid "/feed")
                      access-token
                      {:fields "from,created_time,message,story,to,type,picture,link,icon" :limit 100}))

(defn should-follow? [done-pred result]
  (let [data (:data result)
        next-url (get-in result [:paging :next])]
    (if (and next-url (not (done-pred (last data))))
      next-url)))

(defn urls-to-follow [result-sets done-pred]
  (keep #(should-follow? done-pred %) result-sets))

(defn batch-request [access-token urls]
  (print-vals "BatchRequest of size:" (count urls))
  (let [batch (map #(hash-map :method "GET" :relative_url %) urls)]
    (->> {:query-params {:access_token access-token :batch (json/json-str batch)}}
         (http/post "https://graph.facebook.com/")
         :body
         json/read-json
         (keep (fn [r] (if-let [body (:body r)] (json/read-json body)))))))

(defn process-contact-feeds-batch [access-token results done-pred urls]
  (let [bodies (batch-request access-token urls)
        data (map :data bodies)
        next-urls (urls-to-follow bodies done-pred)]
    (print-vals "Data size:" (apply + (map count data)) "NextUrls of size:" (count next-urls))
    (if (empty? next-urls)
      (apply concat results data)
      (do
        (print-vals "recurring with " (count next-urls))
        (recur access-token (apply concat results data) done-pred next-urls)))))

(defn fetch-contact-feeds [access-token last-updated-string provider-uids]
  (let [done? #(.isAfter (ctc/to-date-time last-updated-string) (ctc/to-date-time (:created_time %)))]
    (->> provider-uids
         (map #(recent-activity-url % access-token))
         (partition-all 25)
         (pmapcat #(process-contact-feeds-batch access-token [] done? %)))))