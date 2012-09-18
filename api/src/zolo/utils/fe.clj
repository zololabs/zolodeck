(ns zolo.utils.fe
  (:use zolodeck.utils.debug))

(defn format-contact [c]
  (let [si (first (:contact/social-identities c))]
    {:name (str (:contact/first-name c) " "
                (:contact/last-name c))
     :guid (str (:contact/guid c))
     :picture-url (:social/thumbnail-url si)}))