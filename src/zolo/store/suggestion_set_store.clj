(ns zolo.store.suggestion-set-store
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]
            [zolo.demonic.core :as demonic]
            [zolo.store.user-store :as u-store]))

;;TODO test 
(defn append-suggestion-set [u suggestion-set]
  (->> suggestion-set
       (conj (:user/suggestion-sets u))
       (assoc (u-store/to-loadable u) :user/suggestion-sets)
       demonic/insert-and-reload))