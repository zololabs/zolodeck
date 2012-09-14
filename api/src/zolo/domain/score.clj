(ns zolo.domain.score
  (:use zolodeck.utils.debug))

(defn calculate [c]
  (* 10 (count (:contact/messages c))))


