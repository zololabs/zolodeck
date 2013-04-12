(ns zolo.social.email.gateway
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [context-io.oauth :as oauth]
            [context-io.api.two :as context-io]))

(def ^:dynamic *creds* (oauth/make-oauth-creds (conf/context-io-key) (conf/context-io-secret)))

(defn get-data- [data-fn account-id limit offset other-params results-key-seq results]
  (let [resp (data-fn *creds* :params (print-vals "GetData:" data-fn
                                                  (merge {:account-id account-id :limit limit :offset offset}
                                                         other-params)))]
    (if (not= 200 (get-in resp [:status :code]))
      results
      (let [cs (get-in resp results-key-seq)
            num (count cs)]
        (if (zero? num)
          results
          (recur data-fn account-id limit (+ offset num) other-params results-key-seq (concat results cs)))))))

(defn get-accounts []
  (context-io/list-accounts *creds*))

(defn get-account [account-id]
  (context-io/get-account *creds* :params {:id account-id}))

(defn get-contacts [account-id date-in-seconds]
  (get-data- context-io/list-account-contacts account-id 500 0 {:active_after date-in-seconds} [:body :matches] []))

(defn get-messages [account-id date-in-seconds]
  (get-data- context-io/list-account-messages account-id 1000 0 {:date_after date-in-seconds} [:body] []))