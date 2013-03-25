(ns zolo.utils.domain.validations-test
  (:use [zolo.utils.domain.validations]
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolodeck.utils.clojure :as zolo-clojure]))

(deftest test-validate-attribute
  (are [expected attribute validators m] (= expected (validate-attribute attribute validators (flatten-keys m)))
       
         ;;Required
         []     [:a]      [:required]      {:a 1}
         []     [:a]      [:required]      {:a ""}
         ["[:a] is required"]  [:a]     [:required]    {:a nil}
         ["[:a] is required"]  [:a]     [:required]    {:b 1}
         
         ;;Nested Required
         []                       [:a :b]   [:required]      {:a {:b 1}}
         ["[:a :b] is required"]  [:a :b]   [:required]      {:a 1}

         ;;Optional
         []     [:a]  [:optional]    {:a 1}
         []     [:a]  [:optional]    {:a nil}
         []     [:a]  [:optional]    {:b 1}

         ;;UUID
         []                    [:a]     [:uuid]   {:a (zolo-clojure/random-guid)}
         ["[:a] is not UUID"]  [:a]     [:uuid]    {:a "addsasda"}
         ["[:a] is not UUID"]  [:a]     [:uuid]    {:a 1}
         ["[:a] is not UUID"]  [:a]     [:uuid]    {:a nil}

         ;; java.util.Date
         []                              [:a]     [:date]    {:a #inst "1980-08-08T00:00:00.000-00:00"}
         ["[:a] is not java.util.Date"]  [:a]     [:date]    {:a "addsasda"}
         ["[:a] is not java.util.Date"]  [:a]     [:date]    {:a 1}
         ["[:a] is not java.util.Date"]  [:a]     [:date]    {:a nil}

         ;;String
         []     [:a]      [:string]      {:a "apple"}
         []     [:a]      [:string]      {:a ""}
         ["[:a] is not string"]  [:a]     [:string]    {:a 1}
         ["[:a] is not string"]  [:a]     [:string]    {:a nil}
         ["[:a] is not string"]  [:a]     [:string]    {:b 1}

         ;; Required String
         ["[:a] is not string"]    [:a]     [:required :string]    {:a 1}
         ["[:a] is required"
          "[:a] is not string"]    [:a]     [:required :string]    {:a nil}

         ;; Optional String
         ["[:a] is not string"]    [:a]     [:optional :string]    {:a 1}
         []                        [:a]     [:optional :string]    {:a nil}

         ;;Integer
         []     [:a]      [:integer]      {:a 1}
         ["[:a] is not integer"]  [:a]      [:integer]      {:a nil}
         ["[:a] is not integer"]  [:a]      [:integer]      {:b 1}
         ["[:a] is not integer"]  [:a]     [:integer]    {:a "one"}
         ["[:a] is not integer"]  [:a]     [:integer]    {:a 1.1}

         ;;Vector
         []     [:a]      [:vector]      {:a []}
         []     [:a]      [:vector]      {:a [:b]}
         ["[:a] is not vector"]  [:a]     [:vector]    {:a 1}
         ["[:a] is not vector"]  [:a]     [:vector]    {:a {:b 1}}
         ["[:a] is not vector"]  [:a]     [:vector]    {:a '(1 2)}
         ["[:a] is not vector"]  [:a]     [:vector]    {:a nil}
         ["[:a] is not vector"]  [:a]     [:vector]    {:b 1}

         ;;Required Vector
         []                         [:a]      [:required :vector]      {:a [:b]}
         []                         [:a]      [:required :vector]      {:a []}
         ["[:a] is required" 
           "[:a] is not vector"]     [:a]      [:required :vector]      {:a nil}

         ;;Optional Vector
         []     [:a]      [:optional :vector]      {:a [:b]}
         []     [:a]      [:optional :vector]      {:a nil}
         []     [:a]      [:optional :vector]      {:a []}

         ;;Empty Not Allowed Vector
         []                         [:a]      [:empty-not-allowed :vector]      {:a [:b]}
         ["[:a] is empty"]          [:a]      [:empty-not-allowed :vector]      {:a []}


         ;;Collection
         []                          [:a]      [:collection]      {:a [:b]}
         []                          [:a]      [:collection]      {:a '(:b)}
         []                          [:a]      [:collection]      {:a #{:b}}
         ["[:a] is not collection"]  [:a]      [:collection]      {:a 1}
))

(deftest test-required-optional
  (testing "One Level Map"
    (let [vs {:a [:required]
              :b [:optional]}]
      
      (are [expected m] (= expected (valid? vs m))
           
           [true []]       {:a "1"}       
           [true []]       {:a "1" :b "2"}
           [false ["It is not a Map"]]       []
           [false ["It is nil!"]]       nil
           [false ["[:a] is required"]]      {}
           [false ["[:a] is required"]]      {:b "2"}
           [false ["[:a] is required"]]      {:a  nil})))

  (testing "Nested Map"
     (let [vs {:a {:a1 [:required]}
               :b [:optional]}]
      
      (are [expected m] (= expected (valid? vs m))

           [true []]       {:a {:a1 "1"}}       
           [false ["[:a :a1] is required"]]      {}
           
           [false ["It is not a Map"]]        []
           [false ["It is nil!"]]        nil

           [false ["[:a :a1] is required"]]      {:b "2"}))))


(deftest test-optional-string
  (let [vs {:a [:required :string]
            :b [:optional :string]}]
      
    (are [expected m] (= expected (valid? vs m))
           
         [true []]                          {:a "1"}       
         [true []]                          {:a "1" :b "two"}
         [false ["[:b] is not string"]]     {:a "1" :b 2}
))) 


(deftest test-extra-fields

  (let [vs {:a {:a1 [:required]}
            :b [:optional]}]
    
    (are [expected m] (= expected (valid? vs m))

       [false ["#{[:c]} - extra keys"]]                           {:a {:a1 1} :c 2}
       [false ["#{[:a]} - extra keys"  "[:a :a1] is required"]]   {:a "1" :b "2"}
       [false ["#{[:a]} - extra keys"  "[:a :a1] is required"]]   {:a  nil})))

