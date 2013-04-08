(ns zolo.social.email.gateway
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [context-io.oauth :as oauth]
            [context-io.api.two :as context-io]))

(def ^:dynamic *creds* (oauth/make-oauth-creds (conf/context-io-key) (conf/context-io-secret)))

(defn get-data- [data-fn account-id limit offset results-key-seq results]
  (let [resp (data-fn *creds* :params (print-vals "GetData:" data-fn
                                                  {:account-id account-id :limit limit :offset offset}))]
    (if (not= 200 (get-in resp [:status :code]))
      results
      (let [cs (get-in resp results-key-seq)
            num (count cs)]
        (if (zero? num)
          results
          (recur data-fn account-id limit (+ offset num) results-key-seq (concat results cs)))))))

(defn get-accounts []
  (context-io/list-accounts *creds*))

(defn get-contacts [account-id]
  (get-data- context-io/list-account-contacts account-id 500 0 [:body :matches] []))

(defn get-messages [account-id]
  (get-data- context-io/list-account-messages account-id 1000 0 [:body] []))