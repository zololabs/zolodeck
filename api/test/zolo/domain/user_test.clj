(ns zolo.domain.user-test
  (:use [zolo.domain.user :as user]
        [zolo.facebook.gateway :as fb-gateway]
        [zolo.facebook.inbox :as fb-inbox]
        [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
        [zolodeck.clj-social-lab.facebook.core :as fb]
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
  (let [fb-user (fb-factory/new-user)]

    (demonic-testing "When there are no friends"
      (stubbing [fb-gateway/friends-list (fb-factory/sample-friends 2)]
        (user/insert-fb-user fb-user)
        (is (empty? (:user/contacts (user/find-by-fb-id (:id fb-user)))))
        (update-facebook-friends (:id fb-user))
        (is (= 2 (count (map :user/first-name (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))

    (demonic-testing "When new friend got added"
      (let [friends (fb-factory/sample-friends 2)]
        (stubbing [fb-gateway/friends-list friends]
          (user/insert-fb-user fb-user)
          (update-facebook-friends (:id fb-user))
          (is (= 2 (count (:user/contacts (user/find-by-fb-id (:id fb-user)))))))
        (stubbing [fb-gateway/friends-list (concat friends (fb-factory/sample-friends 3))]
          (update-facebook-friends (:id fb-user))
          (is (= 5 (count (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))

    (demonic-testing "When a old friend is updated"
      (let [friends (fb-factory/sample-friends 1)]
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

(deftest test-update-inbox
  (demonic-testing "Happy path, with a bunch of conversations"
    (fb/in-facebook-lab
     (let [amit (fb/create-user "Amit" "Rathore")
           deepthi (fb/create-user "Deepthi" "Somasunder")
           siva (fb/create-user "Siva" "Jagadeesan")]
       (user/insert-fb-user amit)

       (fb/make-friend amit deepthi)
       (fb/make-friend amit siva)

       (is (empty? (:user/messages (user/find-by-fb-id (:id amit)))))
       
       (fb/send-message amit deepthi "1" "Hi, what's going on?" "2012-05-01")
       (fb/send-message deepthi amit "1" "Nothing, just work..." "2012-05-02")
       (fb/send-message amit deepthi "1" "OK, should I get groceries?" "2012-05-03")

       (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages amit)]
         (update-facebook-inbox (:id amit))
         (is (= 3 (count (:user/messages (user/find-by-fb-id (:id amit)))))))
       
       (fb/send-message amit siva "2" "Hi, how's  it going?" "2012-06-01")
       (fb/send-message siva amit "2" "Good, I finished writing the tests" "2012-06-02")
       (fb/send-message amit siva "2" "OK, did you update the card?" "2012-06-03")

       (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages amit)]
         (update-facebook-inbox (:id amit))
         (is (= 6 (count (:user/messages (user/find-by-fb-id (:id amit)))))))))))

