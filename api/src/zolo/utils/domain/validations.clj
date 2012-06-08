(ns zolo.utils.domain.validations
  (:use zolodeck.utils.debug))

(defn optional-validator-present? [validators]
  (contains? (set validators) :optional))

(defn uuid? [a]
  (= java.util.UUID (class a)))

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
(def val-empty-not-allowed (validator-fn- (complement empty?) "is empty"))

(def val-uuid (validator-fn- uuid? "is not UUID"))
(def val-string (validator-fn- string? "is not string"))
(def val-integer (validator-fn- integer? "is not integer"))

(def VALIDATOR-KEY-TO-VALIDATOR-FN
     {:required val-required
      :optional val-optional
      :uuid val-uuid
      :string val-string
      :integer val-integer
      :vector val-vector
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
      [false errors]))) 

(defn valid? [validation-map m]
  (if (map? m)
    (valid-map?- (flatten-keys validation-map) (flatten-keys m))
    [false ["It is not a Map"]]))