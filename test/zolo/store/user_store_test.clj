(ns zolo.store.user-store-test
  (:use clojure.test
        zolodeck.utils.debug
        zolodeck.demonic.test)
  (:require [zolo.store.user-store :as u-store]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.personas.factory :as personas]
            [zolo.marconi.facebook.core :as fb-lab]))

(demonictest test-find-by-provider-info
  (personas/in-social-lab
   (let [fb-user1 (fb-lab/create-user "first1" "last1")
         fb-user2 (fb-lab/create-user "first2" "last2")
         db-user1 (personas/create-db-user fb-user1)
         db-user2 (personas/create-db-user fb-user2)]
     
     (is (= db-user2 (u-store/find-by-provider-and-provider-uid :provider/facebook (:id fb-user2))))
     (is (= db-user1 (u-store/find-by-provider-and-provider-uid :provider/facebook (:id fb-user1))))
     
     (is (nil? (u-store/find-by-provider-and-provider-uid :provider/twitter (:id fb-user1))))
     (is (nil? (u-store/find-by-provider-and-provider-uid  nil (:id fb-user1))))
     (is (nil? (u-store/find-by-provider-and-provider-uid :junk (:id fb-user1))))
     (is (nil? (u-store/find-by-provider-and-provider-uid "junk" (:id fb-user1))))
     (is (nil? (u-store/find-by-provider-and-provider-uid :provider/facebook "1000junk"))))))

(demonictest test-find-by-guid
  (personas/in-social-lab
   (let [fb-user1 (fb-lab/create-user "first1" "last1")
         fb-user2 (fb-lab/create-user "first2" "last2")
         db-user1 (personas/create-db-user fb-user1)
         db-user2 (personas/create-db-user fb-user2)]
     
     (is (= db-user2 (u-store/find-by-guid (:user/guid db-user2))))
     (is (= db-user2 (u-store/find-by-guid (str (:user/guid db-user2)))))

     (is (= db-user1 (u-store/find-by-guid (:user/guid db-user1))))
     
     (is (nil? (u-store/find-by-guid (zolodeck.utils.clojure/random-guid-str))))
     (is (nil? (u-store/find-by-guid nil)))

     (is (thrown?  IllegalArgumentException (u-store/find-by-guid "100JUNK"))))))

(deftest test-save
  (demonic-testing "new user saved"
    (personas/in-social-lab
     (let [fb-user (fb-lab/create-user "first1" "last1")
           d-user (personas/create-domain-user fb-user)
           db-user (u-store/save d-user)]

       (db-assert/assert-datomic-user-count 1)
       (db-assert/assert-datomic-user-identity-count 1)

       ;;TODO Make this pass by removing datomic attribs
       ;;(is (= d-user db-user))
       ))))

(demonictest test-reload
  (personas/in-social-lab
   (let [fb-user (fb-lab/create-user "first" "last")
         db-user (personas/create-db-user fb-user)]

     (is (nil? (:user/refresh-started db-user)))

     (u-store/stamp-refresh-start db-user)

     (is (nil? (:user/refresh-started db-user)))
     (is (not (nil? (:user/refresh-started (u-store/reload db-user)))))

     (testing "when user passed is nil it should throw exception"
       (is (thrown?  IllegalArgumentException (u-store/reload nil))))

     (testing "when user passed does not have guid it should throw exception"
       (is (thrown?  IllegalArgumentException (u-store/reload (dissoc db-user :user/guid))))))))


(demonictest test-stamp
  (personas/in-social-lab
   (let [fb-user (fb-lab/create-user "first" "last")
         db-user (personas/create-db-user fb-user)]

     (is (nil? (:user/refresh-started db-user)))
     (is (nil? (:user/last-updated db-user)))

     (let [u-user (u-store/stamp-refresh-start db-user)]
       (is (nil? (:user/last-updated u-user)))
       (is (not (nil? (:user/refresh-started u-user)))))

     (let [u-user (u-store/stamp-updated-time db-user)]
       (is (not (nil? (:user/last-updated u-user))))
       (is (not (nil? (:user/refresh-started u-user))))))

   (testing "when nil is passed it should throw exception"
     (is (thrown?  IllegalArgumentException (u-store/stamp-refresh-start nil)))
     (is (thrown?  IllegalArgumentException (u-store/stamp-updated-time nil))))))
