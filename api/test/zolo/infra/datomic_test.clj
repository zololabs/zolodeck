(ns zolo.infra.datomic-test
  (:use zolo.domain.user 
        zolo.test.core-utils
        [clojure.test :only [run-tests deftest is are testing]]
        [zolo.infra.datomic :only [in-datomic-demarcation delete] :as datomic]
        [zolo.infra.datomic-helper :only [DATOMIC-TEST]]))

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

(defn cleanup-siva []
  (testing "cleanup of database"
    (datomic/in-datomic-demarcation
     (if-let [e-id (:db/id (find-by-fb-id (:id SIVA)))]
       (datomic/delete e-id))
     (is (nil? (:db/id (find-by-fb-id (:id SIVA))))))))

(deftest test-datomic-test-infra
  (testing "nothing exists to start"
    (datomic/in-datomic-demarcation   
     (is (nil? (:db/id (find-by-fb-id "10000"))))))

  (testing "within a test, you can CRUD correctly"
    (binding [DATOMIC-TEST true]
      (datomic/in-datomic-demarcation   
       (is (nil? (:db/id (find-by-fb-id "10000"))))
       (insert-fb-user (assoc SIVA :id "10000"))
       (is-not (nil? (:db/id (find-by-fb-id "10000")))))))

  (testing "after a test, DB is restored"
    (datomic/in-datomic-demarcation   
     (is (nil? (:db/id (find-by-fb-id "10000")))))))

(deftest test-new-user-persistence
  (cleanup-siva)
  
  (testing "regular demarcations do persist at the end"
    (datomic/in-datomic-demarcation   
     (is (nil? (:db/id (find-by-fb-id (:id SIVA)))))
     (insert-fb-user SIVA)
     (is-not (nil? (:db/id (find-by-fb-id (:id SIVA)))))))

  (testing "regular demarcations are permanent"
    (datomic/in-datomic-demarcation   
     (is-not (nil? (:db/id (find-by-fb-id (:id SIVA)))))))

  (cleanup-siva))

