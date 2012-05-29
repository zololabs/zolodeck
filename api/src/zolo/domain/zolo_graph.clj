(ns zolo.domain.zolo-graph
  (:use zolodeck.utils.debug)
  (:require [zolo.utils.validations :as validations]))

;;TODO Zolo ID should be part of value not just in keys

(def MAIN-VALIDATION-MAP
     {:about 
      {:first-name [:required :string]
       :last-name [:required :string]
       :gender [:required :string]
       :facebook {:link [:required :string]
                  :username [:required :string]
                  :email [:required :string] ;;TODO Email format validation 
                  :id [:required :string]
                  :auth-token [:required :string]}}
      :contacts [:required :vector]})

(def CONTACT-VALIDATION-MAP
     {:about 
      {:first-name [:required :string]
       :last-name [:required :string]
       :gender [:required :string]
       :facebook {:id [:required :string]
                  :link [:required :string]
                  :birthday [:required :string] ;;TODO Date Format Validation
                  :picture [:required :string]}}

      :scores [:required :vector]
      
      :messages [:required :vector]})

(def SCORE-VALIDATION-MAP
     {:value [:required :integer] 
      :at [:required :integer]}) ;;TODO Timestamp Format Validation

(def MESSAGE-VALIDATION-MAP
     {:id [:required :string]
      :platform [:required :string] ;;TODO Enum Valur Validation
      :mode [:required :string] ;;TODO Enum Valur Validation
      :text [:required :string]
      :date [:required :integer] ;;TODO Timestamp Format Validation
      :from [:required :string]
      :to [:required :string]
      :thread-id [:optional :string]
      :reply-to [:optional :string]})

(defn main [g]
  (first (vals g)))

(defn contacts [g]
  (map (fn [m] (first (vals m))) (:contacts (main g))))

(defn scores [g]
  (apply concat (map :scores (contacts g))))

(defn messages [g]
  (apply concat (map :messages (contacts g))))

(defn validate-vector- [vm v]
  (reduce (fn [acc m]
            (concat acc (validations/valid? vm m)))
          []
          v))

(defn format-errors- [errors]
  (let [tmp (partition 2 errors)] 
    (map second tmp)))

(defn valid? [g]
  (let [errors (format-errors- (concat []
                                       (validations/valid? MAIN-VALIDATION-MAP (first (vals g)))
                                       (validate-vector- CONTACT-VALIDATION-MAP (contacts g))
                                       (validate-vector- SCORE-VALIDATION-MAP (scores g))
                                       (validate-vector- MESSAGE-VALIDATION-MAP (messages g))))]
    (every? empty? errors)))

(defn load-from-datomic [])


(defn format-for-d3 [g]
  )


