(ns zolo.gigya.core
  (:use zolodeck.utils.debug)
  (:require [clj-http.client :as http-client]
            [zolo.setup.config :as config])
  (:import [org.apache.commons.codec.binary Base64]))

(defn base64encode [value]
  (Base64/encodeBase64String (.getBytes value)))

(def base64-encoded-gigya-info
  (base64encode (str (config/gigya-key) ":" (config/gigya-secret))))

(defn gigya-headers []
  {"Authorization"(str "Basic " base64-encoded-gigya-info)})

(defn access-token []
  (-> (http-client/post "https://socialize.gigya.com/socialize.getToken" 
                        {:headers (gigya-headers) 
                         :content-type "application/x-www-form-urlencoded"
                         :form-params {"grant_type" "none"}
                         :as :json})
      :body
      :access_token))


