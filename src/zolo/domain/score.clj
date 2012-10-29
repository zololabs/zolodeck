(ns zolo.domain.score
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.accessors :as dom]))

(defn calculate [imbc c]
  (* 10 (count (imbc c))))


