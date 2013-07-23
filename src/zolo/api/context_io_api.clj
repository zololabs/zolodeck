(ns zolo.api.context-io-api
  (:use zolo.utils.debug
        zolo.utils.http-status-codes)
  (:require [zolo.utils.logger :as logger]
            [zolo.service.context-io-service :as ci-service]))

(defn get-account [request-params]
  (let [{g-code :google_code callback-url :callback_url} request-params
        u-info (ci-service/user-info g-code callback-url)]
    {:status (STATUS-CODES :ok)
     :body {:cio_account_id (ci-service/context-io-account-id u-info)
            :email (:email u-info)}}))







