(ns zolo.social.email.gateway
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [context-io.oauth :as oauth]
            [context-io.api.two :as context-io]))

(defn context-io-creds []
  (oauth/make-oauth-creds (conf/context-io-key) (conf/context-io-secret)))

(defn- cio-source-params [email access-token refresh-token]
  {:email email
   :server "imap.gmail.com"
   :username email
   :use_ssl 1
   :port 993
   :type "IMAP"
   :sync_period "1h"
   :provider_token access-token
   :provider_refresh_token refresh-token
   :provider_token_secret (conf/google-secret)
   :provider_consumer_key (conf/google-key)})

(defn get-data- [data-fn account-id limit offset other-params results-key-seq results]
  (let [resp (time (data-fn (context-io-creds) :params (merge {:account-id account-id :limit limit :offset offset}
                                                              other-params)))]
    (if (not= 200 (get-in resp [:status :code]))
      results
      (let [cs (get-in resp results-key-seq)
            num (count cs)]
        (if (< num limit) ;(zero? num)
          (concat results cs)
          (recur data-fn account-id limit (+ offset num) other-params results-key-seq (concat results cs)))))))

(defn create-account [email access-token refresh-token]
  (-> (context-io/create-account (context-io-creds) :params (cio-source-params email access-token refresh-token))
      :body))

(defn get-accounts []
  (context-io/list-accounts (context-io-creds)))

(defn get-account [account-id]
  (-> (context-io/get-account (context-io-creds) :params {:id account-id})
      :body))

(defn get-contacts [account-id date-after-in-seconds]
  (get-data- context-io/list-account-contacts account-id 500 0 {:active_after date-after-in-seconds} [:body :matches] []))

(defn get-messages
  ([account-id date-after-in-seconds]
     (get-data- context-io/list-account-messages account-id 1000 0 {:date_after date-after-in-seconds} [:body] []))
  ([account-id date-after-in-seconds date-before-in-seconds]
     (get-data- context-io/list-account-messages account-id 1000 0 {:date_after date-after-in-seconds :date_before date-before-in-seconds} [:body] [])))

(defn- gmail-prefixed [thread-id]
  (if (.startsWith thread-id "gm-")
    thread-id
    (str "gm-" thread-id)))

(defn get-gmail-thread [account-id thread-id]
  (context-io/get-account-thread
   (context-io-creds)
   :params
   {:account-id account-id :thread-id (gmail-prefixed thread-id) :include_body 1}))

(defn get-thread [account-id message-id-in-thread]
  (context-io/list-account-messages-in-thread
   (context-io-creds)
   :params
   {:account-id account-id :message-id (java.net.URLEncoder/encode message-id-in-thread) :include_body 1}))
