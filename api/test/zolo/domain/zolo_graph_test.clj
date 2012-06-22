(ns zolo.domain.zolo-graph-test
  (:use zolodeck.utils.debug
        zolodeck.demonic.test
        [zolo.domain.zolo-graph :as zg]
        [clojure.test :only [run-tests deftest is are testing]]
        zolo.test.assertions)
  (:require [zolo.factories.zolo-graph-factory :as zgf]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.loner :as loner]
            [zolo.personas.core :as personas]
            [zolo.personas.shy :as shy]))

(def contact1 (zgf/new-contact #G"cccc1000"))
(def contact2 (zgf/new-contact #G"cccc2000"))

(deftest test-user-zolo-id
  (is (= #G"aaaa1000" (user-zolo-id (zgf/new-user #G"aaaa1000")))))

(deftest test-contact-zolo-id
  (is (= #G"cccc1000" (user-zolo-id (zgf/new-contact #G"cccc1000")))))

(deftest test-contact-zolo-ids
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/add-contact contact2))]
    (is (= #{#G"cccc1000" #G"cccc2000"} (set (contact-zolo-ids zg))))))

(deftest test-contacts
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/add-contact contact2))]
    (is (= 2 (count (contacts zg))))
    (is (= #G"cccc2000" (:zolo-id (contact zg #G"cccc2000"))))))

(deftest test-messages
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/send-message contact1 "send 1")
            (zgf/receive-message contact1 "recieve 1")
            (zgf/add-contact contact2)
            (zgf/send-message contact2 "send 2")
            (zgf/receive-message contact2 "recieve 2"))]
    (is (= #{"send 2" "recieve 2"} 
           (set (map :text (messages zg #G"cccc2000")))))
    (is (= #{"send 1" "send 2" "recieve 1" "recieve 2"} 
           (set (map :text (all-messages zg)))))))


(deftest test-score
  (testing "When score present"
    (testing "it should return latest score"
      (let [zg (zgf/building 
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1)
                (zgf/add-score contact1 100 5000)
                (zgf/add-score contact1 101 1000)
                (zgf/add-score contact1 200 9000))]
        (is (= true (zg/has-score? zg #G"cccc1000")))
        (is (= {:value 200 :at 9000} (zg/score zg #G"cccc1000")))
        (is (= 200 (zg/score-value (zg/score zg #G"cccc1000")))))))
  
  (testing "when no score is present"
    (testing "it should return nil and -1 for value"
      (let [zg (zgf/building 
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1))]
        (is (= false (zg/has-score? zg #G"cccc1000")))
        (is (nil? (zg/score zg #G"cccc1000")))
        (is (= -1 (zg/score-value (zg/score zg #G"cccc1000"))))))))

(deftest test-scores
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/add-score contact1 100)
            (zgf/add-score contact1 101)
            (zgf/add-contact contact2)
            (zgf/add-score contact2 200)
            (zgf/add-score contact2 201))]
    (is (= #{200 201} 
           (set (map :value (scores zg #G"cccc2000")))))
    (is (= #{100 101 200 201} 
           (set (map :value (all-scores zg)))))))




