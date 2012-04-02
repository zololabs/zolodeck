(ns zolo.infra.datomic-test
  (:use zolo.domain.user zolo.test-utils)
  (:use [clojure.test :only [run-tests deftest is are testing]])
  (:use [zolo.infra.datomic :only [DATOMIC-TEST in-datomic-demarcation] :as datomic]))

(deftest test-datomic-test-infra
  (testing "nothing exists to start"
    (datomic/in-datomic-demarcation   
     (is (nil? (:db/id (find-by-facebook-id "noone@gmail.com"))))))

  (testing "within a test, you can CRUD correctly"
    (binding [DATOMIC-TEST true]
      (datomic/in-datomic-demarcation   
       (is (nil? (:db/id (find-by-facebook-id "noone@gmail.com"))))
       (insert-new-user "no" "one" "noone@gmail.com" "sekrit")
       (is-not (nil? (:db/id (find-by-facebook-id "noone@gmail.com")))))))

  (testing "after a test, DB is restored"
    (datomic/in-datomic-demarcation   
     (is (nil? (:db/id (find-by-facebook-id "noone@gmail.com")))))))

(deftest test-new-user-persistence
  (testing "regular demarcations do persist at the end"
    (datomic/in-datomic-demarcation   
     (is (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com"))))
     (insert-new-user "amit" "rathore" "amitrathore@gmail.com" "sekrit")
     (is-not (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com"))))))

  (testing "regular demarcations are permanent"
    (datomic/in-datomic-demarcation   
     (is-not (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com")))))))

