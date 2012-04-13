(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user] 
        zolo.test-utils
        [clojure.test :only [run-tests deftest is are testing]]
        [org.rathore.amit.conjure.core :as conjure]))

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


(zolotest test-find-by-fb-signed-request
  (testing "when the user is not present in datomic"
    (testing "it should load from fb"
      (conjure/mocking [load-from-fb]
        (assert-datomic-id-not-present (find-by-fb-id (:id SIVA)))
        (user/find-by-fb-signed-request (signed-request-for SIVA)))
      (conjure/verify-call-times-for load-from-fb 1))

    (testing "it should load from fb and save the user to datomic"
      (conjure/stubbing [load-from-fb SIVA]
        (assert-datomic-id-not-present (find-by-fb-id (:id SIVA)))
        (user/find-by-fb-signed-request (signed-request-for SIVA))
        (assert-datomic-id-present (find-by-fb-id (:id SIVA))))))
          
  (testing "when the user is present in datomic"
    (testing "it should NOT load the user from facebook")
    (testing "it should load the user from datomic")))

(zolotest test-load-from-fb 
          (testing "given a signed request, it loads the user from facebook")
)


