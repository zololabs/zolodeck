(ns zolo.social.facebook.gateway
  (:use zolodeck.utils.debug)
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [uri.core :as uri]
            [zolo.setup.config :as conf]
            [zolo.utils.logger :as logger]))

(defn- encoded-request-params [body-map]
  {:content-type "application/x-www-form-urlencoded"
   :accept "application/json"
   :throw-exceptions false
   :body (uri/form-url-encode body-map)})

(defn- me-url []
  "https://graph.facebook.com/me")

(defn- user-info-url [user-id]
  (str "https://graph.facebook.com/"  user-id))

(defn get-json [url access-token query-params]
  (-> (http/get url
                {:query-params (merge {:access_token access-token} query-params)})
      :body
      json/read-json))

(defn- get-json-body [url]
  (-> url http/get :body json/read-json))

(defn- get-pages [payload]
  (let [data (:data payload)
        has-next-url (get-in payload [:paging :next])]
    (if has-next-url
      (lazy-cat data (get-pages (get-json-body has-next-url)))
      data)))

(defn get-json-all-pages [url access-token query-params]
  (-> url
      (get-json access-token query-params)
      get-pages))

(defn run-fql [access-token fql-string]
  (-> (get-json "https://graph.facebook.com/fql" access-token {:q fql-string})
      :data))

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