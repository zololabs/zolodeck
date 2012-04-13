(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user] 
        zolo.test-utils
        [clojure.test :only [run-tests deftest is are testing]]
        [org.rathore.amit.conjure.core]))

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

(deftest test-find-by-fb-signed-request
  (testing "when the user is not present in datomic"
    (zolo-testing "it should load from fb"
      (mocking [load-from-fb]
        (assert-datomic-id-not-present (find-by-fb-id (:id SIVA)))
        (user/find-by-fb-signed-request (signed-request-for SIVA)))
      (verify-call-times-for load-from-fb 1))
    
    (zolo-testing "it should save the user to datomic"
      (stubbing [load-from-fb SIVA]
        (assert-datomic-id-not-present (find-by-fb-id (:id SIVA)))
        (let [user (user/find-by-fb-signed-request (signed-request-for SIVA))]
          (is (= (:gender SIVA) (:user/gender user))))
        (assert-datomic-id-present (find-by-fb-id (:id SIVA))))))
  
  (zolo-testing "when the user is present in datomic"
    (insert-fb-user SIVA)                
    (testing "it should load user from datomic and NOT facebook"
      (assert-datomic-id-present (find-by-fb-id (:id SIVA)))
      (mocking [load-from-fb]
        (let [user (user/find-by-fb-signed-request (signed-request-for SIVA))]
          (is (= (:gender SIVA) (:user/gender user)))))
      (verify-call-times-for load-from-fb 0))))
