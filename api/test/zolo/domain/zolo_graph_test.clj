(ns zolo.domain.zolo-graph-test
  (:use zolodeck.utils.debug
        [zolo.domain.zolo-graph :as zolo-graph]
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.factories.zolo-graph-factory :as zg-factory]
            [zolo.domain.zolo-graph.validation :as zg-validation]))

(deftest test-user-zolo-id
  (is (= "user-100" (user-zolo-id (zg-factory/base "user-100")))))

(deftest test-contact-zolo-id
  (is (= "contact-100" (user-zolo-id (zg-factory/contact "contact-100")))))

(deftest test-contact-zolo-ids
  (let [zg (-> (zg-factory/base)
               (zg-factory/add-contact (zg-factory/contact "contact-100"))
               (zg-factory/add-contact (zg-factory/contact "contact-101"))
               zg-validation/assert-zolo-graph)]
    (is (= #{"contact-100" "contact-101"} (set (contact-zolo-ids zg))))))

(deftest test-contacts
  (let [zg (-> (zg-factory/base)
               (zg-factory/add-contact (zg-factory/contact "contact-100"))
               (zg-factory/add-contact (zg-factory/contact "contact-101"))
               ;;TODO why we need to remember to call this everytime ... something needs to be done
               zg-validation/assert-zolo-graph)]
    (is (= 2 (count (contacts zg))))
    (is (= "contact-101" (:zolo-id (contact zg "contact-101"))))))

(deftest test-messages
  (let [contact1 (-> (zg-factory/contact "contact-100")
                     (zg-factory/add-message {:zolo-id "msg-100"})
                     (zg-factory/add-message {:zolo-id "msg-101"}))

        contact2 (-> (zg-factory/contact "contact-200")
                     (zg-factory/add-message {:zolo-id "msg-200"})
                     (zg-factory/add-message {:zolo-id "msg-201"}))
        zg (-> (zg-factory/base)
               (zg-factory/add-contact contact1)
               (zg-factory/add-contact contact2))]
    (is (= #{"msg-200" "msg-201"} 
           (set (map :zolo-id (messages zg "contact-200")))))
    (is (= #{"msg-200" "msg-201" "msg-100" "msg-101"} 
           (set (map :zolo-id (all-messages zg)))))))

(deftest test-scores
  (let [contact1 (-> (zg-factory/contact "contact-100")
                     (zg-factory/add-score {:value 100})
                     (zg-factory/add-score {:value 101}))

        contact2 (-> (zg-factory/contact "contact-200")
                     (zg-factory/add-score {:value 200})
                     (zg-factory/add-score {:value 201}))
        zg (-> (zg-factory/base)
               (zg-factory/add-contact contact1)
               (zg-factory/add-contact contact2))]
    (is (= #{200 201} 
           (set (map :value (scores zg "contact-200")))))
    (is (= #{100 101 200 201} 
           (set (map :value (all-scores zg)))))))



