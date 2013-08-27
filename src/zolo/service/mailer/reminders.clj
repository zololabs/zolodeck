(ns zolo.service.mailer.reminders
  (:require [zolo.domain.core :as d-core]
            [zolo.domain.user :as user]
            [zolo.domain.user-identity :as ui]
            [zolo.service.contact-service :as c-service]
            [zolo.service.mailer.postman :as postman]))

(defn send-daily-action-reminder [u]
  (d-core/run-in-tz-offset (:user/login-tz u)
     (let [to-email (-> u ui/email-ids first)
           reply-to-count (count (c-service/apply-selectors [c-service/REPLY-TO] 200 0 u))
           follow-up-count (count (c-service/apply-selectors [c-service/FOLLOW-UP] 200 0 u))]
       (postman/deliver-daily-action-reminder to-email
                                              (+ reply-to-count follow-up-count)
                                              (postman/with-base-info u
                                                {:first-name (user/first-name u)
                                                 :reply-to-count reply-to-count
                                                 :follow-up-count follow-up-count})))))