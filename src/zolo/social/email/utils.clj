(ns zolo.social.email.utils
  (:use zolo.utils.debug)
  (:require [clj-http.client :as http]))

(defn rfc822-message [from to reply-to-message-id subject body]
  (-> (str "From: " from
           "\r\nTo: " to
           "\r\nSubject: " subject
           "\r\nIn-Reply-To: " reply-to-message-id
           "\r\nReferences:" reply-to-message-id
           "\r\n\r\n" body)
      print-vals))

(defn encoded-post-param [from subject message]
  (-> {:from from :subject subject :body message}
      http/generate-query-string
      java.net.URLEncoder/encode))