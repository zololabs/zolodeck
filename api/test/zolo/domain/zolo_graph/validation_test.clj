(ns zolo.domain.zolo-graph.validation-test
  (:use zolodeck.utils.debug
        [zolo.domain.zolo-graph.validation :as zg-validation]
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.factories.zolo-graph-factory :as zg-factory]))


(deftest test-zolo-graph-validation
  (testing "With All Fields"
    (testing "It should be valid"
      (is (zg-validation/valid? (-> (zg-factory/user)
                                    (zg-factory/add-contact-with-message-and-score))))))

  (testing "Without no Contact"
    (testing "It should be invalid"
      (is (not (zg-validation/valid? (zg-factory/user))))))

  (testing "Without zolo-id"
    (testing "it should be invalid"
      (is (not (zg-validation/valid? (-> (zg-factory/user nil)
                                         (zg-factory/add-contact)))))))

  (testing "Without Messages"
    (testing "it should be valid"
      (let [zg-contact (-> (zg-factory/contact)
                           zg-factory/add-score)
            zg (-> (zg-factory/user)
                   (zg-factory/add-contact zg-contact))]
        (is (zg-validation/valid? zg)))))

  (testing "Without Scores"
    (testing "it should be valid"
      (let [zg-contact (-> (zg-factory/contact)
                           zg-factory/add-message)
            zg (-> (zg-factory/user)
                   (zg-factory/add-contact zg-contact))]
        (is (zg-validation/valid? zg)))))

  (testing "With invalid Message"
    (testing "it should be Invalid"
      (let [zg-contact (-> (zg-factory/contact)
                           (zg-factory/add-message {:text nil}))
            zg (-> (zg-factory/user)
                   (zg-factory/add-contact zg-contact))]
        (is (not (zg-validation/valid? zg))))))


  (testing "With invalid Score"
    (testing "it should be Invalid"
      (let [zg-contact (-> (zg-factory/contact)
                           (zg-factory/add-score {:value "JUNK"}))
            zg (-> (zg-factory/user)
                   (zg-factory/add-contact zg-contact))]
        (is (not (zg-validation/valid? zg)))))))

