(ns zolo.service.mailer.postman
  (:use zolo.utils.debug)
  (:require [zolo.setup.config :as conf]
            [clojurewerkz.mailer.core :as mailer]))

(defn with-base-info [u data-map]
  (merge {:host "smtp.sendgrid.net"
          :port 587
          :user (conf/sendgrid-username)
          :pass (conf/sendgrid-password)
          :user-guid (:user/guid u)
          :kiss-api (conf/kiss-api-key)}
         data-map))

(defn deliver-email [to-email subject template data]
  (mailer/deliver-email {:from "Zolodeck Assistant <assistant@zolodeck.com>" :to [to-email] :subject subject} template data :text/html))

(defn deliver-daily-action-reminder [to-email total-contacts-count data]
  (deliver-email to-email (str total-contacts-count " contacts need action today") "emails/daily_actions.mustache" data))