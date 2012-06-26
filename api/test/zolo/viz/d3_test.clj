(ns zolo.viz.d3-test
  (:use zolodeck.utils.debug
        [zolo.domain.zolo-graph :as zg]
        [zolo.viz.d3 :as d3]
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.factories.zolo-graph-factory :as zgf]))

(def contact1 (zgf/new-contact #G"cccc1000"))
(def contact2 (zgf/new-contact #G"cccc2000"))

(deftest test-format-for-d3

  (testing "With one Contact"
    (testing "Score not yet calculated"
      (let [zg (zgf/building
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1))]
        (is (= {"nodes" [{"name" "aaaa1000-1000-413f-8a7a-f11c6a9c4036" 
                          "group" 1000}]
                "links" []}
               (d3/format-for-d3 zg)))))

    (testing "Score is calculated"
      (let [zg (zgf/building
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1)
                (zgf/add-score contact1 20))]
        (is (= {"nodes" [{"name" "aaaa1000-1000-413f-8a7a-f11c6a9c4036" 
                          "group" 1000}
                         {"name" "cccc1000-1000-413f-8a7a-f11c6a9c4036_fname - 20"
                          "group" 1}]
                "links" [{"source" 0
                          "target" 1
                          "value" -20}]}
               (d3/format-for-d3 zg))))))


  (testing "With more than one view Contact"
    (testing "None of the Scores are calculated"
      (let [zg (zgf/building
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1)
                (zgf/add-contact contact2))]
        (is (= {"nodes" [{"name" "aaaa1000-1000-413f-8a7a-f11c6a9c4036" "group" 1000}]
                "links" []}
               (d3/format-for-d3 zg)))))

    (testing "Not all Scores are calculated"
      (let [zg (zgf/building
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1)
                (zgf/add-score contact1 20)
                (zgf/add-contact (zgf/new-contact))
                (zgf/add-contact contact2)
                (zgf/add-score contact2 40))]
        (is (= {"nodes" [{"name" "aaaa1000-1000-413f-8a7a-f11c6a9c4036" "group" 1000}
                         {"name" "cccc2000-1000-413f-8a7a-f11c6a9c4036_fname - 40" "group" 1}
                         {"name" "cccc1000-1000-413f-8a7a-f11c6a9c4036_fname - 20" "group" 1}]
                "links" [{"source" 0 "target" 1 "value" -40}
                         {"source" 0 "target" 2 "value" -20}]}
               (d3/format-for-d3 zg)))))

    (testing "All Scores are calculated"
      (let [zg (zgf/building
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1)
                (zgf/add-score contact1 20)
                (zgf/add-contact contact2)
                (zgf/add-score contact2 40))]
        (is (= {"nodes" [{"name" "aaaa1000-1000-413f-8a7a-f11c6a9c4036" "group" 1000}
                         {"name" "cccc2000-1000-413f-8a7a-f11c6a9c4036_fname - 40" "group" 1}
                         {"name" "cccc1000-1000-413f-8a7a-f11c6a9c4036_fname - 20" "group" 1}]
                "links" [{"source" 0 "target" 1 "value" -40}
                         {"source" 0 "target" 2 "value" -20}]}
               (d3/format-for-d3 zg)))))))
