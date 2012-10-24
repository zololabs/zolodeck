(ns zolo.utils.fe
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.accessors :as dom]
            [clj-time.core :as time])
  (:import org.joda.time.DateTime))

(defn days-not-contacted [u c]
  (let [messages (dom/contact-messages u c)]
    (if (empty? messages)
      -1
      (let [ts (->> messages
                    (map :message/date)
                    sort
                    last
                    .getTime
                    DateTime.)
            n (time/now)
            i (time/interval ts n)]
        (time/in-days i)))))

(defn format-contact [u c]
  (let [si (first (:contact/social-identities c))]
    {:name (str (:contact/first-name c) " "
                (:contact/last-name c))
     :guid (str (:contact/guid c))
     :picture-url (:social/photo-url si)
     :days-not-contacted (days-not-contacted u c)
     }))