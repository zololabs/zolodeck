(ns zolo.utils.domain.validations
  (:use zolodeck.utils.debug)
  (:require [zolodeck.utils.clojure :as zolo-clj]))

(defn optional-validator-present? [validators]
  (contains? (set validators) :optional))

(defn validator-fn- [func msg]
  (fn [m attribute validators]
    (when-not (and 
               (optional-validator-present? validators)
               (nil? (m attribute)))
      (when-not (func (m attribute))
        (str attribute " " msg)))))

(def val-optional (constantly nil))
(def val-required (validator-fn- (complement nil?) "is required"))

(def val-vector (validator-fn- vector? "is not vector"))
(def val-collection (validator-fn- zolo-clj/collection? "is not collection"))
(def val-empty-not-allowed (validator-fn- (complement empty?) "is empty"))

(def val-uuid (validator-fn- zolo-clj/uuid? "is not UUID"))
(def val-date (validator-fn- zolo-clj/date? "is not java.util.Date"))
(def val-string (validator-fn- string? "is not string"))
(def val-integer (validator-fn- integer? "is not integer"))

(def VALIDATOR-KEY-TO-VALIDATOR-FN
     {:required val-required
      :optional val-optional
      :uuid val-uuid
      :date val-date
      :string val-string
      :integer val-integer
      :vector val-vector
      :collection val-collection
      :empty-not-allowed val-empty-not-allowed})

(defn flatten-keys* [a ks m]
  (if (and (map? m) (not (empty? m)))
    (reduce into (map (fn [[k v]] (flatten-keys* a (conj ks k) v)) (seq m)))
    (assoc a ks m)))

(defn flatten-keys [m] 
  (if (empty? m)
    {}
    (flatten-keys* {} [] m)))

(defn diff-keys [from to]
  (apply disj (set (keys from)) (keys to)))

(defn validate-attribute [attribute validators m]
  (reduce (fn [errors validator]
            (if-let [error ((VALIDATOR-KEY-TO-VALIDATOR-FN validator) m attribute validators)]
              (conj errors error)
              errors))
          []
          validators))

(defn validate-attributes [flattened-validation-map flattened-map]
  (reduce (fn [errors [attribute validators]] 
            (concat errors 
                    (validate-attribute attribute validators flattened-map)))
          []
          flattened-validation-map))

(defn validate-keys [flattened-validation-map flattened-map]
  (let [extra-keys (diff-keys flattened-map flattened-validation-map)]
    (when-not (empty? extra-keys)
      (vector (str extra-keys " - extra keys")))))

(defn valid-map?- [flattened-validation-map flattened-map]
  (let [errors (concat
                (validate-keys flattened-validation-map flattened-map)
                (validate-attributes flattened-validation-map flattened-map))]
    (if (empty? errors)
      [true []]
      [false (sort errors)]))) 

(defn valid? [validation-map m]
  (cond 
   (nil? m) [false ["It is nil!"]]
   (map? m) (valid-map?- (flatten-keys validation-map) (flatten-keys m))
   :else [false ["It is not a Map"]]))