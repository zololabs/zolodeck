(ns zolo.gigya.core
  (:use zolodeck.utils.debug)
  (:require [clj-http.client :as http-client]
            [zolo.setup.config :as config])
  (:import [org.apache.commons.codec.binary Base64]))

(defn base64encode [value]
  (Base64/encodeBase64String (.getBytes value)))

(def base64-encoded-gigya-info
  (base64encode (str (config/gigya-key) ":" (config/gigya-secret))))

(defn gigya-basic-headers []
  {"Authorization"(str "Basic " base64-encoded-gigya-info)})

(defn gigya-oauth-headers [access-token]
  {"Authorization"(str "OAuth " access-token)})

(defn access-token []
  (-> (http-client/post "https://socialize.gigya.com/socialize.getToken" 
                        {:headers (gigya-basic-headers) 
                         :content-type "application/x-www-form-urlencoded"
                         :form-params {"grant_type" "none"}
                         :as :json})
      :body
      :access_token
      (print-vals-> "Access Token :")))


(defn notify-registration [user gigya-user]
  (http-client/post "https://socialize.gigya.com/socialize.notifyRegistration" 
                    {:headers (gigya-oauth-headers (access-token)) 
                     :debug true
                     :form-params {"siteUID" (str (:user/guid user))
                                   "UID" (:UID gigya-user)
                                   "format" "json"}})
  user)

(defn delete-account [site-uid]
    (-> (http-client/post "https://socialize.gigya.com/socialize.deleteAccount" 
                        {:headers (gigya-oauth-headers (access-token)) 
                         :debug true
                         :form-params {"UID" site-uid
                                       "format" "json"}})
      (print-vals-> "Response from Delete Account:")))