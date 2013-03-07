(ns zolo.utils.fe
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.accessors :as dom]
            [clj-time.core :as time])
  (:import org.joda.time.DateTime))

(defn days-not-contacted [c ibc]
  (let [interactions (ibc c)]
    (if (empty? interactions)
      -1
      (let [ts (->> (dom/messages-from-interactions interactions)                    
                    (keep dom/message-date)
                    last
                    .getTime
                    DateTime.)
            n (time/now)
            i (time/interval ts n)]
        (time/in-days i)))))

(defn format-contact [ibc c]
  (let [si (first (:contact/social-identities c))]
    {:name (str (:contact/first-name c) " " (:contact/last-name c))
     :guid (str (:contact/guid c))
     :muted (:contact/muted c)
     :picture-url (:social/photo-url si)
     :days-not-contacted (days-not-contacted c ibc)
     :contacted-today (:contacted-today c)
     }))

(defn format-contact-info [c]
  (let [si (first (:contact/social-identities c))]
    {:name (str (:contact/first-name c) " " (:contact/last-name c))
     :guid (str (:contact/guid c))
     :muted (:contact/muted c)
     :picture-url (:social/photo-url si)
     :contacted-today (:contacted-today c)
     }))