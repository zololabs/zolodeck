(ns zolo.domain.score
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.accessors :as dom]))

(defn calculate [ibc c]
  (* 10 (count (ibc c))))


