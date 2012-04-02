(ns zolo.infra.datomic-test
  (:use zolo.domain.user zolo.test-utils)
  (:use [clojure.test :only [run-tests deftest is are testing]])
  (:use [zolo.infra.datomic :only [insert-new run-query load-entity in-datomic-demarcation] :as datomic]))

(deftest test-new-user-persistence
  (datomic/init-db)
  (datomic/in-datomic-demarcation   
   (is (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com")))))
  (datomic/in-datomic-demarcation   
   (is (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com"))))
   (insert-new-user "amit" "rathore" "amitrathore@gmail.com" "sekrit")
   (is-not (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com")))))
  (datomic/in-datomic-demarcation   
   (is-not (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com")))))  )