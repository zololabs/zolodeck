(ns zolo.service.core
  (:use zolo.utils.debug
        [slingshot.slingshot :only [throw+]])
  (:require [zolo.utils.domain.validations :as validations]
            [zolo.domain.user :as user]))

(defn validate-request! [request-params validation-map]
  (let [[valid? messages] (validations/valid? validation-map request-params)]  
    (if valid?
      request-params
      (throw+ {:type :bad-request :error messages}))))