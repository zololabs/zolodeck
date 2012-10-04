(ns zolo.web.fb-auth
  (:use [clojure.data.json :only [read-json]])
  (:require [clojure.string :as str])
  (:import [org.apache.commons.codec.binary Base64]
           [javax.crypto Mac]
           [javax.crypto.spec SecretKeySpec]))

(defn strtr
  "My take on PHP's strtr function."
  [value from to]
  ((apply comp (map (fn [a b] #(.replace % a b)) from to))
   value))

(defn base64-decode
  "Decodes a base64 string, convenience wrapper around Java library."
  [base64]
  (String. (Base64/decodeBase64 base64)))

(defn hmac-sha-256
  "Returns a HMAC-SHA256 hash of the provided data."
  [^String key ^String data]
  (let [hmac-key (SecretKeySpec. (.getBytes key) "HmacSHA256")
        hmac (doto (Mac/getInstance "HmacSHA256") (.init hmac-key))]
    (String. (org.apache.commons.codec.binary.Base64/encodeBase64
              (.doFinal hmac (.getBytes data)))
             "UTF-8")))

(defn decode-signed-request
  "Takes a Facebook signed_request parameter and the applications secret
  key and returns a payload hash or nil if there was a problem."
  [signed-request key]
  (when (and signed-request key
             (re-matches #"^[^\.]+\.[^\.]+$" signed-request))
    (let [[signiture payload] (str/split signed-request #"\.")
          signiture (str (strtr signiture "-_" "+/") "=")]
      (when (= signiture (hmac-sha-256 key payload))
        (read-json (base64-decode payload))))))
