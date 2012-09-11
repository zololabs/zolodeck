(ns zolo.utils.http
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

(defn get-gigya-access-token []
  (-> (http-client/post "https://socialize.gigya.com/socialize.getToken" 
                        {:headers (gigya-basic-headers) 
                         :content-type "application/x-www-form-urlencoded"
                         :form-params {"grant_type" "none"}
                         :as :json})
      :body
      :access_token))

(def GIGYA-ACCESS-TOKEN (get-gigya-access-token))

(defn gigya-oauth-post
  ([path]
     (gigya-oauth-post path {}))
  ([path form-params]
     (-> (http-client/post (str "https://socialize.gigya.com/" path) 
                           {:headers (gigya-oauth-headers GIGYA-ACCESS-TOKEN)
                            :content-type "application/x-www-form-urlencoded"
                            :form-params (merge {"format" "json"} form-params)
                            :as :json})
         :body)))