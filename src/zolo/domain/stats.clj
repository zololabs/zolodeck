(ns zolo.domain.stats
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.utils.fe :as fe]            
            [zolo.domain.message :as message]
            [zolo.domain.score :as score]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.math :as zolo-math]))

(defn contacts-with-score-between [u lower upper]
  (filter #(and (>= (:contact/score %) lower)
                (< (:contact/score %) upper))
          (:user/contacts u)))

(defn contacts-stats [u]
  {:total (count (:user/contacts u))
   :strong (count (contacts-with-score-between u 250 10000000))
   :medium (count (contacts-with-score-between u 50 250))
   :weak (count (contacts-with-score-between u 0 50))})

(defn message-count [u]
  (print-vals "MessageCount for" (:user/first-name u))
  (->> u
       :user/contacts
       (mapcat :contact/messages)
       count))

(defn network-stats [u]
  {:average (zolo-math/average (map :contact/score (:user/contacts u)))
   :messagecount (message-count u)
   ;;TODO This needs to be tested
   :strong-contacts (doall (map fe/format-contact (contact/strong-contacts u 5)))   
   :weak-contacts (doall (map fe/format-contact (contact/weak-contacts u 5)))
   :connect-soon (doall (map fe/format-contact (contact/forgotten-contacts u true 5)))
   :never-contacted (doall (map fe/format-contact (contact/forgotten-contacts u false 5)))
   :all-week-interaction-count (user/all-message-count-in-the-past u 7)
   :all-month-interaction-count (user/all-message-count-in-the-past u 31) 
   })