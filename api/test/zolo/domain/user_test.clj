(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user]
        [zolo.facebook.gateway :as fb-gateway]
        [zolodeck.clj-social-lab.facebook.data-factory :as fb-factory]
        zolodeck.demonic.test
        zolo.test.core-utils
        zolodeck.utils.debug
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


(deftest test-update-friends-list
  (let [fb-user (fb-factory/user)]

    (demonic-testing "When there are no friends"
      (stubbing [fb-gateway/friends-list (fb-factory/friends 2)]
        (user/insert-fb-user fb-user)
        (is (empty? (:user/contacts (user/find-by-fb-id (:id fb-user)))))
        (update-facebook-friends (:id fb-user))
        (is (= 2 (count (map :user/first-name (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))

    (demonic-testing "When new friend got added"
      (let [friends (fb-factory/friends 2)]
        (stubbing [fb-gateway/friends-list friends]
          (user/insert-fb-user fb-user)
          (update-facebook-friends (:id fb-user))
          (is (= 2 (count (:user/contacts (user/find-by-fb-id (:id fb-user)))))))
        (stubbing [fb-gateway/friends-list (concat friends (fb-factory/friends 3))]
          (update-facebook-friends (:id fb-user))
          (is (= 5 (count (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))

    (demonic-testing "When a old friend is updated"
      (let [friends (fb-factory/friends 1)]
        (stubbing [fb-gateway/friends-list friends]
          (user/insert-fb-user fb-user)
          (update-facebook-friends (:id fb-user))
          (is (= (:first_name (first friends)) 
                 (:contact/first-name (first (:user/contacts (user/find-by-fb-id (:id fb-user))))))))
        (let [updated-friend (assoc (first friends) :first_name "NewName")]
          (stubbing [fb-gateway/friends-list [updated-friend]]
            (update-facebook-friends (:id fb-user))
            (is (= "NewName"                  
                   (:contact/first-name (first (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))))))

