(ns zolo.social.facebook.gateway
  (:use zolodeck.utils.debug
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [zolodeck.utils.maps :as maps]
            [uri.core :as uri]
            [zolo.setup.config :as conf]
            [zolo.utils.logger :as logger]))

(def FACEBOOK-BATCH-SIZE 50)

(defn- encoded-request-params [body-map]
  {:content-type "application/x-www-form-urlencoded"
   :accept "application/json"
   :throw-exceptions false
   :body (uri/form-url-encode body-map)})

(defn- me-url []
  "https://graph.facebook.com/me")

(defn- user-info-url [user-id]
  (str "https://graph.facebook.com/"  user-id))

(defn recent-activity-url [user-id]
  (if (empty? user-id)
    (throw+ {:type :missing-argument
             :message "user-id must be specified when calling recent-activity-url"}))
  (str "https://graph.facebook.com/" user-id "/feed"))

(defn create-url [url access-token query-params-map]
  (->> query-params-map
       (merge {:access_token access-token})
       http/generate-query-string
       (str url "?")))

(defn get-json [url access-token query-params]
  (-> (http/get url
                {:query-params (merge {:access_token access-token} query-params)})
      :body
      json/read-json))

(defn- get-json-body [url]
  (-> url http/get :body json/read-json))

(defn- get-pages [payload items-done-tester-fn]
  (let [data (:data payload)
        has-next-url (get-in payload [:paging :next])]
    (print-vals "Get-pages, data payload count:" (count data))
    (if (and has-next-url (not (items-done-tester-fn (last data))))
      (lazy-cat data (get-pages (get-json-body has-next-url) items-done-tester-fn))
      data)))

(defn get-json-all-pages [url access-token query-params]
  (-> url
      (get-json access-token query-params)
      (get-pages (constantly false))))

(defn get-json-pages-until [url access-token query-params items-done-tester-fn]
  (-> url
      (get-json access-token query-params)
      (get-pages items-done-tester-fn)))

(defn get-json-first-page [url access-token query-params]
  (-> url
      (get-json access-token query-params)
      (get-pages (constantly false))))

(defn run-fql [access-token fql-string]
  (print-vals "RunFQL: " fql-string)
  (-> (get-json "https://graph.facebook.com/fql" access-token {:q fql-string})
      :data))

(defn run-fql-multi [access-token fql-strings-as-map]
  ;; (print-vals "RunMultiFQL: " fql-strings-as-map)
  (->> {:q (json/json-str fql-strings-as-map)}
       (get-json "https://graph.facebook.com/fql" access-token)
       :data))

(defn process-fql-multi
  "result-processor accepts two arguments: name of the query and the corresponding query result"
  [access-token result-processor fql-strings-as-map]
  (let [batches (maps/partition-into FACEBOOK-BATCH-SIZE fql-strings-as-map)]
    (->> batches
         (mapcat #(run-fql-multi access-token %))
         (mapcat (fn [{query-name :name results :fql_result_set}]
                   (result-processor query-name results))))))

(defn user-info [access-token user-id]
  (get-json (user-info-url user-id) access-token {}))

(defn me-info [access-token]
  (get-json (me-url) access-token {}))

(defn- extended-user-info-fql-for [user-id]
  (str "select uid, first_name, last_name, username, sex, birthday_date, locale, current_location, email, pic_small, pic_big, profile_url from user where uid = '" user-id "'"))

(defn extended-user-info [access-token user-id]
  (-> (run-fql access-token (extended-user-info-fql-for user-id))
      first))

(defn friends-list [access-token user-id]
  (get-json-all-pages "https://graph.facebook.com/me/friends" access-token
                      {:fields "id,first_name,last_name,gender,locale,link,username,installed,bio,birthday,education,email,hometown,interested_in,location,picture,relationship_status,significant_other,website"}))