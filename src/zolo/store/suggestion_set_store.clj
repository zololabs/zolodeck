(ns zolo.store.suggestion-set-store
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolodeck.demonic.core :as demonic]
            [zolo.store.user-store :as u-store]))

;;TODO Test this
(defn append-suggestion-set [u suggestion-set]
  (->> suggestion-set
       (conj (:user/suggestion-sets u))
       (assoc u :user/suggestion-sets)
       demonic/insert-and-reload))