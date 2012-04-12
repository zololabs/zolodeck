(ns zolo.domain.user-test
  (:use zolo.domain.user zolo.test-utils)
  (:use [clojure.test :only [run-tests deftest is are testing]]))

(def SIVA {:gender "male",
           :last_name "Jagadeesan",
           :link "http://www.facebook.com/sivajag",
           :timezone -7,
           :name "Siva Jagadeesan",
           :locale "en_US",
           :username "sivajag",
           :email "sivajag@gmail.com",
           :updated_time "2012-02-17T17:36:14+0000",
           :first_name "Siva",
           :verified true,
           :id "1014524783"})

(zolotest test-new-user-persistence
  (is (nil? (:db/id (find-by-fb-id (:id SIVA)))))
  (insert-fb-user SIVA)
  (is-not (nil? (:db/id (find-by-fb-id (:id SIVA))))))

(zolotest test-load-user-from-datomic
  (insert-fb-user SIVA)
  (let [user-from-db (find-by-fb-id (:id SIVA))]
    (is (= (:gender SIVA) (:user/gender user-from-db)))))





