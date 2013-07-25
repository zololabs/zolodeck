(ns zolo.social.email.utils
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [clj-http.client :as http]))

(defn random-string []
  (.replaceAll (random-guid-str) "-" "."))

(defn rfc822-message [from to reply-to-message-id subject body]
  (str "From: " from
       "\r\nTo: " to
       "\r\nSubject: " subject
       "\r\nIn-Reply-To: " reply-to-message-id
       "\r\nReferences:" reply-to-message-id
       "\r\nMessage-ID: <" (random-string) ".mailer@api.zolodeck.com>"
       "\r\n\r\n" body))

(defn encoded-post-param [from subject message]
  (-> {:from from :subject subject :body message}
      http/generate-query-string
      java.net.URLEncoder/encode))

(defn in-folder? [message folder]
  (some #{folder} (:folders message)))

(defn is-spam-or-trash? [message]
  (or (in-folder? message "\\Trash")
      (in-folder? message "\\Spam")))

(defn remove-spam-and-trash [messages]
  (remove is-spam-or-trash? messages))