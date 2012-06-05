(ns zolo.domain.zolo-graph-test
  (:use zolodeck.utils.debug
        [zolo.domain.zolo-graph :as zg]
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.factories.zolo-graph-factory :as zgf]))

(let [main (zgf/new-user "main")
      contact1 (zgf/new-contact "contact1")
      contact2 (zgf/new-contact "contact2")]

  (deftest test-user-zolo-id
    (is (= "user-100" (user-zolo-id (zgf/new-user "user-100")))))

  (deftest test-contact-zolo-id
    (is (= "contact-100" (user-zolo-id (zgf/new-contact "contact-100")))))

  (deftest test-contact-zolo-ids
    (let [zg (zgf/building 
              main
              (zgf/add-contact contact1)
              (zgf/add-contact contact2))]
      (is (= #{"contact1" "contact2"} (set (contact-zolo-ids zg))))))

  (deftest test-contacts
    (let [zg (zgf/building 
              main
              (zgf/add-contact contact1)
              (zgf/add-contact contact2))]
      (is (= 2 (count (contacts zg))))
      (is (= "contact2" (:zolo-id (contact zg "contact2"))))))

  (deftest test-messages
    (let [zg (zgf/building 
              main
              (zgf/add-contact contact1)
              (zgf/send-message contact1 "send 1")
              (zgf/receive-message contact1 "recieve 1")
              (zgf/add-contact contact2)
              (zgf/send-message contact2 "send 2")
              (zgf/receive-message contact2 "recieve 2"))]
      (is (= #{"send 2" "recieve 2"} 
             (set (map :text (messages zg "contact2")))))
      (is (= #{"send 1" "send 2" "recieve 1" "recieve 2"} 
             (set (map :text (all-messages zg)))))))

  (deftest test-scores
    (let [zg (zgf/building 
              main
              (zgf/add-contact contact1)
              (zgf/add-score contact1 100)
              (zgf/add-score contact1 101)
              (zgf/add-contact contact2)
              (zgf/add-score contact2 200)
              (zgf/add-score contact2 201))]
      (is (= #{200 201} 
             (set (map :value (scores zg "contact2")))))
      (is (= #{100 101 200 201} 
             (set (map :value (all-scores zg)))))))

  (deftest test-score
    (testing "When score present"
      (testing "it should return latest score"
        (let [zg (zgf/building 
                  main
                  (zgf/add-contact contact1)
                  (zgf/add-score contact1 100 5000)
                  (zgf/add-score contact1 101 1000)
                  (zgf/add-score contact1 200 9000))]
          (is (= {:value 200 :at 9000} (zg/score zg "contact1")))
          (is (= 200 (zg/score-value (zg/score zg "contact1"))))))))

  (deftest test-format-for-d3
    (testing "With one Contact"
      (testing "Score not yet calculated")
      (testing "Score is calculated"
        (let [zg (zgf/building
                  main
                  (zgf/add-contact contact1)
                  (zgf/add-score contact1 20))]
          (is (= {"nodes" [{"name" "main" 
                            "group" 1000}
                           {"name" "contact1"
                            "group" 1}]
                  "links" [{"source" 0
                            "target" 1
                            "value" 20}]}
                 (zg/format-for-d3 zg))))))))



