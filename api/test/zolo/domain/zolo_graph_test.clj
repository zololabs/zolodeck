(ns zolo.domain.zolo-graph-test
  (:use zolodeck.utils.debug
        [zolo.domain.zolo-graph :as zolo-graph]
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.factories.zolo-graph-factory :as zg-factory]
            [zolo.domain.zolo-graph.validation :as zg-validation]))

(deftest test-contacts
  (let [zg (-> (zg-factory/base)
               (zg-factory/add-contact (zg-factory/contact "contact-100"))
               (zg-factory/add-contact (zg-factory/contact "contact-101"))
               ;;TODO why we need to remember to call this everytime ... something needs to be done
               zg-validation/assert-zolo-graph)]
    (is (= 2 (count (contacts zg))))
    (is (= "contact-101" (:zolo-id (contact zg "contact-101"))))))

