(ns zolo.utils.fe
  (:use zolodeck.utils.debug))

(defn format-contact [c]
  {:name (str (get-in c [:about :first-name]) " "
              (get-in c [:about :last-name]))
   :guid (str (:guid c))
   :picture-url (get-in c [:about :facebook :picture])})