(ns zolo.domain.user-test
  (:use zolo.domain.user zolo.test-utils)
  (:use [clojure.test :only [run-tests deftest is are testing]])
  (:use [zolo.setup.datomic :only [insert-new run-query load-entity] :as datomic]))

(deftest test-new-user-persistence
  (datomic/init-db)
  (is (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com"))))
  (insert-new-user "amit" "rathore" "amitrathore@gmail.com" "sekrit")
  (is-not (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com")))))