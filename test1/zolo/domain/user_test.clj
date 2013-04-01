(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user]
        [zolo.facebook.gateway :as fb-gateway]
        [zolo.marconi.facebook.factory :as fb-factory]
        [zolo.marconi.facebook.core :as fb]
        zolodeck.demonic.test
        zolo.test.core-utils
        zolo.test.assertions
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.factories.zolo-graph-factory :as zgf]
            [zolo.marconi.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.loner :as loner]
            [zolo.personas.core :as personas]
            [zolo.personas.shy :as shy]))

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
  (user/insert-fb-user SIVA)
  (is-not (nil? (:db/id (find-by-fb-id (:id SIVA))))))

(demonictest test-load-user-from-datomic
  (testing "when passing with a valid fb-id"
    (user/insert-fb-user SIVA)
    (let [user-from-db (find-by-fb-id (:id SIVA))]
      (is (= (:gender SIVA) (:user/gender user-from-db)))))

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
          (is (= (:gender SIVA) (:user/gender user))))
        (assert-datomic-id-present (find-by-fb-id (:id SIVA))))))
  
  (demonic-testing "when the user is present in datomic"
    (insert-fb-user SIVA)                
    (testing "it should load user from datomic and NOT facebook"
      (assert-datomic-id-present (find-by-fb-id (:id SIVA)))
      (mocking [load-from-fb]
        (let [user (user/find-by-fb-signed-request (signed-request-for SIVA))]
          (is (= (:gender SIVA) (:user/gender user)))))
      (verify-call-times-for load-from-fb 0))))

(deftest test-update-scores
  (demonic-testing "when there are no scores before"
    (let [vincent (vincent/create)]
      
      (user/update-scores vincent)
      
      (let [vincent-reloaded (user/reload vincent)
            jack-reloaded (personas/friend-of vincent-reloaded "jack")
            jack-scores (:contact/scores jack-reloaded)]
        
        (is (= 1 (count jack-scores)))
        (is (= 30 (:score/value (first jack-scores))))
        (is (not (nil? (:score/at (first jack-scores))))))))
  
  (demonic-testing "when there are scores already present"
    (let [vincent (vincent/create)]
      (user/update-scores vincent)

      (-> (user/reload vincent)
          user/update-scores)
      
      (let [vincent-reloaded (user/reload vincent)
            jack-reloaded (personas/friend-of vincent-reloaded "jack")
            jack-scores (:contact/scores jack-reloaded)]
        
        (is (= 2 (count jack-scores)))))))