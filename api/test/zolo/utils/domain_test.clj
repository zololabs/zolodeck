(ns zolo.utils.domain-test
  (:use zolodeck.utils.debug
        zolodeck.utils.test
        [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.domain))

(deftest test-group-first-by
  (let [l [{:a 1 :c [100 200]}
           {:a 2 :b [5 6] :c [101 201]}
           {:a 3 :b [15 16]}]]

    (is (= {1 {:a 1 :c [100 200]}
            2 {:a 2 :b [5 6] :c [101 201]}
            3 {:a 3 :b [15 16]}}
           (group-first-by :a l)))))