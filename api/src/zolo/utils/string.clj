(ns zolo.utils.string
  (:import [java.util.regex Pattern]))

(defn split [re s]
  ;; In clj 1.4 this is not working 
  ;;(clojure.string/split #" " auth-token)
  (seq  (.split (Pattern/compile re)  s)))