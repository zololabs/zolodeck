(ns zolo.service.context-io-service
  (:use zolo.utils.debug)
  (:require [zolo.utils.logger :as logger]
            [clj-http.client :as http]
            [zolo.social.email.gateway :as e-gateway]
            [zolo.utils.string :as zstring]
            [clojure.data.json :as json]
            [zolo.utils.maps :as maps]
            [zolo.setup.config :as conf]
            [zolo.store.user-identity-store :as ui-store]
            [zolo.service.core :as service]))

(defn exchange-code-for-token [code callback-url]
  (-> (http/post "https://accounts.google.com/o/oauth2/token"
                 {:form-params {:code code
                                :client_id (conf/google-key)
                                :client_secret (conf/google-secret)
                                :redirect_uri callback-url
                                :grant_type "authorization_code"}
                  :throw-entire-message? true})
      :body
      json/read-json))

(defn google-user-info [token-info]
  (let [{it :id_token} token-info]
    (-> (http/get "https://www.googleapis.com/oauth2/v1/tokeninfo"
                  {:query-params {:id_token it}
                   :throw-entire-message? true})
        :body
        json/read-json
        (merge token-info))))

(defn user-info [g-code callback-url]
  (-> g-code
      (exchange-code-for-token callback-url)
      google-user-info))

(def val-request
  {:email [:required :string]
   :access_token [:required :string]
   :refresh_token [:required :string]
   :token_type [:optional :string]
   :expires_in [:optional :integer]
   :issued_to [:optional :string]
   :issued_at [:optional :integer]
   :id_token [:optional :string]
   :user_id [:optional :string]
   :verified_email [:optional :boolean]
   :audience [:optional :string]
   :issuer [:optional :string]})

(defn context-io-account-id [g-user-info]
  (let [{email :email at :access_token rt :refresh_token} g-user-info
        ui (ui-store/find-by-provider-and-email :provider/email email)]
    (if ui
      (:identity/auth-token ui)
      (do
        (service/validate-request! g-user-info val-request)
        (-> (e-gateway/create-account email at rt) :id)))))