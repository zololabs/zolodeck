(ns zolo.domain.score
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolodeck.utils.debug)
  (:require [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.domain :as utils-domain]
            [zolodeck.demonic.core :as demonic]
            [clojure.set :as set]))

(defn calculate [c]
  (* 10 (count (:contact/messages c))))

;;TODO Write Test for this fn
(defn create [c]
  {:score/value (calculate c)
   :score/at (zolo-cal/now-instant)})


