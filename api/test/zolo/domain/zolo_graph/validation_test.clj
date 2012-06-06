(ns zolo.domain.zolo-graph.validation-test
  (:use zolodeck.utils.debug
        [zolo.domain.zolo-graph.validation :as zg-validation]
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.factories.zolo-graph-factory :as zgf]))


(deftest test-zolo-graph-validation
  (let [main (zgf/new-user "main")
        contact1 (zgf/new-contact "contact1")]
    
    (testing "With All Fields"
      (testing "It should be valid"
        (is (zg-validation/valid?
             (zgf/building 
              main
              (zgf/add-contact contact1)
              (zgf/send-message contact1 "hey")
              (zgf/add-score contact1 20))))))
    
    (testing "Without no Contact"
      (testing "It should be invalid"
        (is (thrown? AssertionError (zgf/building main)))))
    
    (testing "Without zolo-id"
      (testing "it should be invalid"
        (is (thrown? AssertionError (zgf/building 
                                     (zgf/new-user nil)
                                     (zgf/add-contact contact1))))))
    
    (testing "Without Messages"
      (testing "it should be valid"
        (is (zg-validation/valid? (zgf/building 
                                   main
                                   (zgf/add-contact contact1)
                                   (zgf/add-score contact1 20))))))    
  
    (testing "Without Scores"
      (testing "it should be valid"
        (is (zg-validation/valid? (zgf/building 
                                   main
                                   (zgf/add-contact contact1)
                                   (zgf/send-message contact1 "hey"))))))


    (testing "With invalid Message"
      (testing "it should be Invalid"
        (is (thrown? AssertionError (zgf/building 
                                     main
                                     (zgf/add-contact contact1)
                                     (zgf/send-message contact1 nil)
                                     (zgf/add-score contact1 20))))))


    (testing "With invalid Score"
      (testing "it should be Invalid"
        (is (thrown? AssertionError (zgf/building 
                                     main
                                     (zgf/add-contact contact1)
                                     (zgf/send-message contact1 "hey")
                                     (zgf/add-score contact1 "JUNK"))))))))