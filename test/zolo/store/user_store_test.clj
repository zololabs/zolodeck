(ns zolo.store.user-store-test
  (:use clojure.test
        zolodeck.utils.debug
        zolodeck.demonic.test)
  (:require [zolo.store.user-store :as u-store]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.personas.factory :as personas]
            [zolo.personas.shy :as shy-persona]
            [zolodeck.clj-social-lab.facebook.core :as fb-lab]))

(demonictest test-find-by-provider-info
  (personas/in-social-lab
   (let [fb-user1 (fb-lab/create-user "first1" "last1")
         fb-user2 (fb-lab/create-user "first2" "last2")
         db-user1 (personas/create-db-user fb-user1)
         db-user2 (personas/create-db-user fb-user2)]
     
     (is (= db-user2 (u-store/find-by-provider-and-provider-uid :provider/facebook (:id fb-user2))))
     (is (= db-user1 (u-store/find-by-provider-and-provider-uid :provider/facebook (:id fb-user1))))
     
     (is (nil? (u-store/find-by-provider-and-provider-uid :provider/twitter (:id fb-user1))))
     (is (nil? (u-store/find-by-provider-and-provider-uid "junk" (:id fb-user1))))
     (is (nil? (u-store/find-by-provider-and-provider-uid :provider/facebook "1000junk"))))))

(deftest test-save
  (demonic-testing "new user saved"
    (let [shy (shy-persona/create)
          db-shy (u-store/save shy)]

      (db-assert/assert-datomic-user-count 1)
      (db-assert/assert-datomic-user-identity-count 1)

      ;;TODO Make this pass by removing datomic attribs
      ;;(is (= shy db-shy))
      )))

