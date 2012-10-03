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


(defn network-stats [u]
  {:average (zolo-math/average (map :contact/score (:user/contacts u)))
   ;;TODO This needs to be tested
   :weak-contacts (doall (map fe/format-contact (take 5 (sort-by :contact/score (:user/contacts u)))))})