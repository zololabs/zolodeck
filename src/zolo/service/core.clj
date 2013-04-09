(ns zolo.service.core
  (:use zolo.utils.debug
        [slingshot.slingshot :only [throw+]])
  (:require [zolo.utils.domain.validations :as validations]
            [zolo.domain.user :as user]
            [zolo.social.core :as social]))

(defn validate-request! [request-params validation-map]
  (let [[valid? messages] (validations/valid? validation-map request-params)]  
    (if valid?
      request-params
      (throw+ {:type :bad-request :error messages}))))

(defn provider-string->provider-enum [provider-string]
  (or (social/provider-enum (.toUpperCase provider-string))
      (throw (RuntimeException. (str "Unknown provider string specified: " provider-string)))))