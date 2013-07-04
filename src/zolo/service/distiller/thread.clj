(ns zolo.service.distiller.thread
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.user :as u]
            [zolo.domain.contact :as c]
            [zolo.domain.thread :as t]
            [clojure.string :as str]
            [zolo.service.distiller.message :as m-distiller]
            [zolo.service.distiller.contact :as c-distiller]))

(defn- lm-contact [u last-message selector-fn]
  (it-> last-message
        (c/find-by-provider-and-provider-uid u (:message/provider it) (selector-fn it))
        (c-distiller/distill-basic it)))

(defn- lm-from-to [u last-m]
  (if (:message/sent last-m)
    (lm-contact u last-m #(first (:message/to %)))
    (lm-contact u last-m :message/from)))

(defn- person-name [p]
  (str (:reply-to/first-name p) " " (:reply-to/last-name p)))

(defn- subject-from-people [distilled-message]
  (it-> distilled-message
        (:message/reply-to it)
        (map person-name it)
        (str/join ", " it)
        (str "Conversation with " it)))

(defn distill [u thread]
  (when thread
    (let [distilled-msgs (domap #(m-distiller/distill u (u/tz-offset-minutes) %) (:thread/messages thread))]
      {:thread/guid (-> distilled-msgs last :message/message-id)
       :thread/subject (or (:thread/subject thread)
                           (-> distilled-msgs first subject-from-people))
       :thread/lm-from-contact (lm-from-to u (first distilled-msgs))
       :thread/ui-guid (-> distilled-msgs first :message/ui-guid)
       :thread/provider (-> thread :thread/messages first :message/provider)
       :thread/messages distilled-msgs})))

(defn distill-by-contacts [u threads]
  (group-by :thread/lm-from-contact (domap #(distill u %) threads)))