(ns zolo.service.distiller.thread
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.domain.user :as u]
            [zolo.domain.contact :as c]
            [zolo.domain.thread :as t]
            [clojure.string :as str]
            [zolo.service.distiller.social-identity :as si-distiller]
            [zolo.service.distiller.message :as m-distiller]))

;;TODO because of cyclic dependency this function is here instead of c-distiller
(defn distill-contact-basic [contact]
  {:contact/first-name (c/first-name contact)
   :contact/last-name (c/last-name contact)
   :contact/guid (:contact/guid contact)
   :contact/muted (c/is-muted? contact)
   :contact/person (c/is-a-person? contact)
   :contact/picture-url (c/picture-url contact)
   :contact/social-identities (map si-distiller/distill (:contact/social-identities contact))})

(defn- lm-contact [u last-message selector-fn]
  (it-> last-message
        (c/find-by-provider-and-provider-uid u (:message/provider it) (selector-fn it))
        (distill-contact-basic it)))

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

(defn distill [u thread & expansions]
  (when thread
    (let [distilled-msgs (domap #(m-distiller/distill u (u/tz-offset-minutes) %) (:thread/messages thread))
          basic-thread       {:thread/guid (-> distilled-msgs last :message/message-id)
                              :thread/subject (or (:thread/subject thread)
                                                  (-> distilled-msgs first subject-from-people))
                              :thread/lm-from-contact (lm-from-to u (first distilled-msgs))
                              :thread/ui-guid (-> distilled-msgs first :message/ui-guid)
                              :thread/provider (-> thread :thread/messages first :message/provider)}]
      (if (some #{"include_messages"} expansions)
        (assoc basic-thread :thread/messages distilled-msgs)
        basic-thread))))

(defn distill-by-contacts [u threads]
  (group-by :thread/lm-from-contact (domap #(distill u % "include_messages") threads)))