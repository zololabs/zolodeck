(ns zolo.twitter.inbox
  (:require [zolo.utils.http :as gigya-gateway])
  (:use [slingshot.slingshot :only [throw+ try+]]
        zolodeck.utils.debug
        zolodeck.utils.calendar
        zolodeck.utils.clojure))

(defn get-twitter-messages [u]
  (gigya-gateway/gigya-raw-data-post {"provider" "twitter"
                                      "UID" (:user/guid u)
                                      "fields" "direct_messages"}))