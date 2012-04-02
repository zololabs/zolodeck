(ns zolo.domain.user-test
  (:use zolo.domain.user zolo.test-utils)
  (:use [clojure.test :only [run-tests deftest is are testing use-fixtures]])
  (:use [zolo.infra.datomic :only [datomic-fixture] :as datomic]))

(zolotest test-new-user-persistence
  (is (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com"))))
  (insert-new-user "amit" "rathore" "amitrathore@gmail.com" "sekrit")
  (is-not (nil? (:db/id (find-by-facebook-id "amitrathore@gmail.com")))))

