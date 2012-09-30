(ns zolo.social.utils)

;; TODO create and move a 'split-at' function into utils.clojure
(defn split-birthdate [mmddyyyy]
  (if mmddyyyy
    (->> (.split mmddyyyy "/")
         (into []))))

(defn split-locale [locale]
  (if locale
    (->> (.split locale "_")
         (into []))))