(ns zolo.domain.score
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.accessors :as dom]))

(defn calculate [u c]
  (* 10 (count (dom/contact-messages u c))))


