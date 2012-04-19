(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user]
        zolodeck.demonic.test
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core))

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

(demonictest test-new-user-persistence
  (is (nil? (:db/id (find-by-fb-id (:id SIVA)))))
  (insert-fb-user SIVA)
  (is-not (nil? (:db/id (find-by-fb-id (:id SIVA))))))

(demonictest test-load-user-from-datomic
  (testing "when passing with a valid fb-id"
    (insert-fb-user SIVA)
    (let [user-from-db (find-by-fb-id (:id SIVA))]
      (is (= (:gender SIVA) (:gender user-from-db)))))

  (testing "when passing nil"
    (is (nil? (find-by-fb-id nil)))))

(deftest test-find-by-fb-signed-request
  (testing "when the user is not present in datomic"
    (demonic-testing "it should load from fb"
      (mocking [load-from-fb]
        (assert-datomic-id-not-present (find-by-fb-id (:id SIVA)))
        (user/find-by-fb-signed-request (signed-request-for SIVA)))
      (verify-call-times-for load-from-fb 1))
    
    (demonic-testing "it should save the user to datomic"
      (stubbing [load-from-fb SIVA]
        (assert-datomic-id-not-present (find-by-fb-id (:id SIVA)))
        (let [user (user/find-by-fb-signed-request (signed-request-for SIVA))]
          (is (= (:gender SIVA) (:gender user))))
        (assert-datomic-id-present (find-by-fb-id (:id SIVA))))))
  
  (demonic-testing "when the user is present in datomic"
    (insert-fb-user SIVA)                
    (testing "it should load user from datomic and NOT facebook"
      (assert-datomic-id-present (find-by-fb-id (:id SIVA)))
      (mocking [load-from-fb]
        (let [user (user/find-by-fb-signed-request (signed-request-for SIVA))]
          (is (= (:gender SIVA) (:gender user)))))
      (verify-call-times-for load-from-fb 0)))

  (deftest test-transform-to-regular-map
    (demonic-testing "reading from datomic, should return a transformed map"
      (insert-fb-user SIVA)
      (let [from-datomic (user/find-by-fb-id (:id SIVA))]
        (is (= (:first_name SIVA) (:first-name from-datomic)))
        (is (= (:last_name SIVA)  (:last-name from-datomic)))
        (is (= (:gender SIVA)     (:gender from-datomic)))
        (is (= (:link SIVA)       (:link from-datomic)))
        (is (= (:username SIVA)   (:username from-datomic)))
        (is (= (:email SIVA)      (:email from-datomic)))
        (is (= (:id SIVA)         (:id from-datomic)))
        (is (= (:auth-token SIVA) (:auth-token from-datomic)))))))

