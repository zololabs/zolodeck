(ns zolo.utils.http
  (:require [clj-http.client :as http-client]
            [zolo.setup.config :as config]
            [clojure.data.json :as json])
  (:import [org.apache.commons.codec.binary Base64]))

(declare GIGYA-ACCESS-TOKEN)

(defn base64encode [value]
  (Base64/encodeBase64String (.getBytes value)))

(def base64-encoded-gigya-info
  (base64encode (str (config/gigya-key) ":" (config/gigya-secret))))

(defn gigya-basic-headers []
  {"Authorization"(str "Basic " base64-encoded-gigya-info)})

(defn gigya-oauth-headers [access-token]
  {"Authorization"(str "OAuth " access-token)})

(defn gigya-post
  ([path]
     (gigya-post path (gigya-oauth-headers GIGYA-ACCESS-TOKEN) {}))
  ([path headers]
     (gigya-post path headers {}))  
  ([path headers form-params]
     (-> (http-client/post (str "https://socialize.gigya.com/" path) 
                           {:headers headers
                            :content-type "application/x-www-form-urlencoded"
                            :form-params (merge {"format" "json"} form-params)
                            :as :json})
         :body)))

(defn get-gigya-access-token []
  (-> (gigya-post "socialize.getToken" (gigya-basic-headers) {"grant_type" "none"})
      :access_token))

(def GIGYA-ACCESS-TOKEN (get-gigya-access-token))

(defn gigya-oauth-post
  ([path]
     (gigya-oauth-post path {}))
  ([path form-params]
     (gigya-post path (gigya-oauth-headers GIGYA-ACCESS-TOKEN) form-params)))

(defn gigya-raw-data-post [form-params]
  (-> (gigya-oauth-post "socialize.getRawData" form-params)
      :data
      json/read-json))