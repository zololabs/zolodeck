(ns zolo.utils.validations-test
  (:use [zolo.utils.validations]
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]))

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

         ;;String
         []     [:a]      [:string]      {:a "apple"}
         []     [:a]      [:string]      {:a ""}
         []     [:a]      [:string]      {:a nil}
         []     [:a]      [:string]      {:b 1}
         ["[:a] is not string"]  [:a]     [:string]    {:a 1}

         ;; Required String
         ["[:a] is not string"]  [:a]     [:required :string]    {:a 1}
         ["[:a] is required"]    [:a]     [:required :string]    {:a nil}

         ;;Integer
         []     [:a]      [:integer]      {:a 1}
         []     [:a]      [:integer]      {:a nil}
         []     [:a]      [:integer]      {:b 1}
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

         ;;Empty Not Allowed
         []                    [:a]      [:empty-not-allowed]      {:a [:b]}
         ["[:a] is empty"]     [:a]      [:empty-not-allowed]      {:a []}

))


(deftest test-required-optional
  (testing "One Level Map"
    (let [vs {:a [:required]
              :b [:optional]}]
      
      (are [expected m] (= expected (valid? vs m))
           
           [true []]       {:a "1"}       
           [true []]       {:a "1" :b "2"}
           [false ["It is not a Map"]]       []
           [false ["It is not a Map"]]       nil
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
           [false ["It is not a Map"]]        nil

           [false ["[:a :a1] is required"]]      {:b "2"}))))


(deftest test-optional-string
  (let [vs {:a [:required :string]
            :b [:optional :string]}]
      
    (are [expected m] (= expected (valid? vs m))
           
         [true []]                          {:a "1"}       
         [true []]                          {:a "1" :b "two"}
         [false ["[:b] is not string"]]     {:a "1" :b 2}))) 


(deftest test-extra-fields

  (let [vs {:a {:a1 [:required]}
            :b [:optional]}]
    
    (are [expected m] (= expected (valid? vs m))

       [false ["#{[:c]} - extra keys"]]                           {:a {:a1 1} :c 2}
       [false ["#{[:a]} - extra keys"  "[:a :a1] is required"]]   {:a "1" :b "2"}
       [false ["#{[:a]} - extra keys"  "[:a :a1] is required"]]   {:a  nil})))

