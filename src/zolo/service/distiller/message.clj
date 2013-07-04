(ns zolo.service.distiller.message
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.message :as m]
            [zolo.domain.user-identity :as ui]
            [zolo.domain.social-identity :as si]))

(defn- author-for-distillation [u message is-sent]
  (if is-sent
    (let [author-ui (ui/find-by-provider-uid u (m/message-from message))]
      {:author/first-name (:identity/first-name author-ui)
       :author/last-name (:identity/last-name author-ui)
       :author/picture-url (:identity/photo-url author-ui)})
    (let [author-si (si/find-by-provider-uid u (m/message-from message))]
      {:author/first-name (:social/first-name author-si)
       :author/last-name (:social/last-name author-si)
       :author/picture-url (:social/photo-url author-si)})))

(defn- reply-to-for-distillation-from-to [u message]
  ;; [to *] minus author 
  (->> (m/message-to message)
       (m/remove-user-from-reply-to u)
       (map #(si/find-by-provider-uid u %))
       (map (fn [si] {:reply-to/first-name (:social/first-name si)
                     :reply-to/last-name (:social/last-name si)
                     :reply-to/provider-uid (:social/provider-uid si)
                     :reply-to/ui-provider-uid (:social/ui-provider-uid si)}))))

(defn- reply-to-for-distillation-from-from [u message is-sent]
  (if (not is-sent)
    (let [from-si (si/find-by-provider-uid u (m/message-from message))]
      {:reply-to/first-name (:social/first-name from-si)
       :reply-to/last-name (:social/last-name from-si)
       :reply-to/provider-uid (:social/provider-uid from-si)
       :reply-to/ui-provider-uid (:social/ui-provider-uid from-si)})))

(defn distill
  ([u message]
     (distill u nil message))
  ([u tz-offset-minutes message]
     (let [is-sent (m/is-sent-by-user? u message)]
       (-> {}
           (assoc :message/message-id (m/message-id message))
           (assoc :message/guid (m/message-guid message))
           (assoc :message/provider (m/message-provider message))
           (assoc :message/thread-id (m/thread-id message))
           (assoc :message/from (m/message-from message))
           (assoc :message/to (m/message-to message))
           (assoc :message/done (m/message-done? message))
           (assoc :message/follow-up-on (m/follow-up-on message))
           (assoc :message/date (m/message-date message))
           (assoc :message/subject (m/message-subject message))
           (assoc :message/ui-guid (m/message-ui-guid message))
           (assoc :message/text (m/message-text message))
           (assoc :message/snippet (m/snippet message))
           (assoc :message/sent is-sent)
           (assoc :message/author (author-for-distillation u message is-sent))
           (assoc :message/reply-to (remove nil?
                                            (conj (reply-to-for-distillation-from-to u message)
                                                  (reply-to-for-distillation-from-from u message is-sent))))))))