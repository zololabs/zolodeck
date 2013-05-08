(ns zolo.api.context-io-api
  (:use zolo.utils.debug
        zolo.web.status-codes)
  (:require [zolo.utils.logger :as logger]
            [clj-http.client :as http]
            [zolo.social.email.gateway :as e-gateway]
            [zolo.utils.string :as zstring]
            [zolo.setup.config :as conf]
            [clojure.data.json :as json]
            [zolo.utils.maps :as maps]
            [zolo.store.user-store :as u-store]))

;;TODO Clean up this namespace and also write test

(defn exchange-code-for-token [code callback-url]
  (-> (http/post "https://accounts.google.com/o/oauth2/token"
                 {:form-params {:code code
                                :client_id (conf/google-key)
                                :client_secret (conf/google-secret)
                                :redirect_uri callback-url
                                :grant_type "authorization_code"}
                  :throw-entire-message? true})
      :body
      json/read-json
      (print-vals-> "Access Token Info")))

(defn google-user-info [token-info]
  (let [{it :id_token} token-info]
    (-> (http/get "https://www.googleapis.com/oauth2/v1/tokeninfo"
                  {:query-params {:id_token it}
                   :throw-entire-message? true})
        :body
        json/read-json
        (print-vals-> "Google User Info")
        (merge token-info))))

(defn user-info [g-code callback-url]
  (-> g-code
      (exchange-code-for-token callback-url)
      google-user-info))

(defn context-io-account-id [g-user-info]
  ;;; Load user from Datomic
  (let [{email :email at :access_token rt :refresh_token} g-user-info]
    (-> (e-gateway/create-account email at rt)
        :id)))

(defn get-account [request-params]
  (let [{g-code :google_code callback-url :callback_url} request-params
        u-info (user-info g-code callback-url)]
    {:status (STATUS-CODES :ok)
     :body (print-vals "Ci ACCOUNT ID" {:ci_account_id (context-io-account-id u-info)})}))







