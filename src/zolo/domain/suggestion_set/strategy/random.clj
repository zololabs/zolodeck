(ns zolo.domain.suggestion-set.strategy.random
  (:use zolo.utils.debug))

(defn compute [u]
  (take 5 (:user/contacts u)))