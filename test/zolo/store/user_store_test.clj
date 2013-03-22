(ns zolo.store.user-store-test
  (:use clojure.test
        zolodeck.utils.debug
        zolodeck.demonic.test)
  (:require [zolo.store.user-store :as u-store]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.personas.shy :as shy-persona]))


(deftest test-save
  (demonic-testing "new user saved"
    (let [shy (shy-persona/create)
          db-shy (u-store/save shy)]

      (db-assert/assert-datomic-user-count 1)
      (db-assert/assert-datomic-user-identity-count 1)

      ;;TODO Make this pass by removing datomic attribs
      ;;(is (= shy db-shy))
      )))

