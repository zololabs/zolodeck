(ns zolo.domain.zolo-graph.validation
  (:use zolodeck.utils.debug)
  (:require [zolo.utils.domain.validations :as validations]
            [zolo.domain.zolo-graph :as zg]))

(defn main-validation-map [u-id]
  {u-id
   {:zolo-id [:required :uuid]
    :about 
    {:first-name [:required :string]
     :last-name [:required :string]
     :gender [:required :string]
     :facebook {:link [:required :string]
                :username [:optional :string]
                :email [:required :string] 
                :id [:required :string]
                :auth-token [:required :string]}}
    :contacts {}
    }})

(defn contact-validation-map [c-id]
  {c-id
   {:zolo-id [:required :uuid]
    :about 
    {:first-name [:required :string]
     :last-name [:required :string]
     :gender [:required :string]
     :facebook {:id [:required :string]
                :link [:required :string]
                :birthday [:required :string] 
                :picture [:required :string]}}

    :scores [:required :vector]
      
    :messages [:required :vector]}})

(defn add-contact-validation-map [val-map u-id c-id]
  (update-in val-map
             [u-id :contacts]
             #(merge % (contact-validation-map c-id)))) 

(defn zg-validation-map [zg]
  (reduce (fn [acc c-id]
            (add-contact-validation-map acc (zg/user-zolo-id zg) c-id))
          (main-validation-map (zg/user-zolo-id zg))
          (zg/contact-zolo-ids zg)))

(def SCORE-VALIDATION-MAP
     {:value [:required :integer] 
      :at [:required :integer]}) 

(def MESSAGE-VALIDATION-MAP
     {:zolo-id [:required :uuid]
      :platform [:required :string] 
      :mode [:required :string] 
      :text [:required :string]
      :date [:required :integer] 
      :from [:required :uuid]
      :to [:required :uuid]
      :thread-id [:optional :string]
      :reply-to [:optional :string]})


(defn validate-vector- [vm v]
  (reduce (fn [acc m]
            (concat acc (validations/valid? vm m)))
          []
          v))

(defn format-errors- [errors]
  (let [tmp (partition 2 errors)] 
    (map second tmp)))

(defn valid? [zg]
  (let [errors (format-errors- (concat []
                                       (validations/valid? (zg-validation-map zg) zg)
                                       (validate-vector- SCORE-VALIDATION-MAP (zg/all-scores zg))
                                       (validate-vector- MESSAGE-VALIDATION-MAP (zg/all-messages zg))
                                       ))
        valid (every? empty? errors)]

    (when-not valid 
      (print-vals "ZOLO-GRAPH Validation Errors : " errors))
    
    valid))


(defn assert-zolo-graph [zg]
  (assert (valid? zg))
  zg)