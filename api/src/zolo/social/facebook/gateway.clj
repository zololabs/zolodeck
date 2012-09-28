(ns zolo.social.facebook.gateway
  (:use zolodeck.utils.debug)
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [uri.core :as uri]
            [zolo.setup.config :as conf]))

(defn encoded-request-params [body-map]
  {:content-type "application/x-www-form-urlencoded"
   :accept "application/json"
   :throw-exceptions false
   :body (uri/form-url-encode body-map)})

;; (defn app-access-token-url []  
;;   "https://graph.facebook.com/oauth/access_token")

;; (defn access-token-request [app-id app-secret]
;;   (encoded-request-params {:grant_type "client_credentials"
;;                            :client_id app-id
;;                            :client_secret app-secret}))

;; (defn app-access-token [app-id app-secret]
;;   (print-vals "Getting App Access Token")
;;   (->> (access-token-request app-id app-secret) 
;;        (http/post (app-access-token-url))
;;        :body
;;        uri/form-url-decode
;;        :access_token))

;(def APP-ACCESS-TOKEN (app-access-token (conf/app-id) (conf/app-secret)))

(defn me-url []
  "https://graph.facebook.com/me")

(defn user-info-url [user-id]
  (str "https://graph.facebook.com/"  user-id))

(defn get-json [url access-token query-params]
  (-> (http/get (print-vals "url:" url)
                {:query-params (print-vals "qparams:" (merge {:access_token access-token} query-params))})
      :body
      json/read-json))

(defn run-fql [access-token fql-string]
  (get-json "https://graph.facebook.com/fql" access-token {:q fql-string}))

(defn user-info [access-token user-id]
  (get-json (user-info-url user-id) access-token {}))

(defn me-info [access-token]
  (get-json (me-url) access-token {}))

(defn extended-user-info-fql-for [user-id]
  (str "select first_name, last_name, username, sex, birthday_date, locale, hometown_location, email, pic_small, pic_big, profile_url from user where uid = '" user-id "'"))

(defn extended-user-info [access-token user-id]
  (run-fql access-token (extended-user-info-fql-for user-id)))