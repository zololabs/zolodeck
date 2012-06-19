(ns zolo.domain.contact-test
  (:use zolodeck.demonic.test
        zolo.test.core-utils
        zolodeck.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.core :as personas]
            [zolo.test.assertions :as assertions]))

(deftest test-update-friends-list
  (let [fb-user (fb-factory/new-user)]

    (demonic-testing "When there are no friends"
      (stubbing [fb-gateway/friends-list (fb-factory/sample-friends 2)]
        (user/insert-fb-user fb-user)
        (is (empty? (:user/contacts (user/find-by-fb-id (:id fb-user)))))
        (user/update-facebook-friends (:id fb-user))
        (is (= 2 (count (map :user/first-name (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))

    (demonic-testing "When new friend got added"
      (let [friends (fb-factory/sample-friends 2)]
        (stubbing [fb-gateway/friends-list friends]
          (user/insert-fb-user fb-user)
          (user/update-facebook-friends (:id fb-user))
          (is (= 2 (count (:user/contacts (user/find-by-fb-id (:id fb-user)))))))
        (stubbing [fb-gateway/friends-list (concat friends (fb-factory/sample-friends 3))]
          (user/update-facebook-friends (:id fb-user))
          (is (= 5 (count (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))

    (demonic-testing "When a old friend is updated"
      (let [friends (fb-factory/sample-friends 1)]
        (stubbing [fb-gateway/friends-list friends]
          (user/insert-fb-user fb-user)
          (user/update-facebook-friends (:id fb-user))
          (is (= (:first_name (first friends)) 
                 (:contact/first-name (first (:user/contacts (user/find-by-fb-id (:id fb-user))))))))
        (let [updated-friend (assoc (first friends) :first_name "NewName")]
          (stubbing [fb-gateway/friends-list [updated-friend]]
            (user/update-facebook-friends (:id fb-user))
            (is (= "NewName"                  
                   (:contact/first-name (first (:user/contacts (user/find-by-fb-id (:id fb-user)))))))))))))

(deftest test-find-contact-by-user-and-fb-id
  (testing "When main user is not present"
    (demonic-testing "it should return nil"
      (let [vincent (vincent/create)
            jack (personas/friend-of vincent "jack")]

        (is (nil? (contact/find-by-user-and-contact-fb-id nil (:contact/fb-id jack)))))))

  (testing "When contact is not present"
    (demonic-testing "it should return nil"
      (let [vincent (vincent/create)]
        (is (nil? (contact/find-by-user-and-contact-fb-id vincent "JUNK-CONTACT-FB-ID"))))))

  (testing "When some contact is associated"

    (testing "with only one user"
      (demonic-testing "it should return the correct contact"
        (let [vincent (vincent/create)
              jack (personas/friend-of vincent "jack")
              jack-from-db (contact/find-by-user-and-contact-fb-id vincent (:contact/fb-id jack))]

          (assertions/assert-contacts-are-same jack jack-from-db))))

      (testing "with two different users"
        (demonic-testing "it should return the correct contact"
          (fb/in-facebook-lab
           (let [user1 (personas/create-fb-user "User" "1")
                 user2 (personas/create-fb-user "User" "2")
                 friend (personas/create-fb-user "Friend" "Original")
                 friend-fb-id (:id friend)]

             (user/insert-fb-user user1)
             (user/insert-fb-user user2)

             (fb/make-friend user1 friend)
             (personas/update-fb-friends user1)

             (fb/update-user friend-fb-id {:last_name "Modified" :name "Friend Modified"})
             (fb/make-friend user2 friend)

             (personas/update-fb-friends user2)

             (let [friend_of_user1 (contact/find-by-user-and-contact-fb-id
                                    (user/find-by-fb-id (:id user1)) friend-fb-id)
                   friend_of_user2 (contact/find-by-user-and-contact-fb-id
                                    (user/find-by-fb-id (:id user2)) friend-fb-id)]

               (is (= "Original" (:contact/last-name friend_of_user1)))
               (is (= "Modified" (:contact/last-name friend_of_user2))))))))))

(deftest test-contact->zolo-contact

  (testing "when nil is passed")
  
  (demonic-testing "when valid contact is passed"
    (let [jack (->> (vincent/create)
                    :user/contacts
                    (sort-by :contact/first-name)
                    first)
          jack-zg (contact/contact->zolo-contact jack)]

      (are [expected key-seq] (= expected (get-in jack-zg key-seq))

           (jack :contact/guid)            [:zolo-id]
           (jack :contact/first-name)      [:about :first-name]
           (jack :contact/last-name)       [:about :last-name]
           (jack :contact/gender)          [:about :gender]
           (jack :contact/fb-id)           [:about :facebook :id]           
           (jack :contact/fb-link)         [:about :facebook :link]           
           (jack :contact/fb-birthday)     [:about :facebook :birthday]           
           (jack :contact/fb-picture-link) [:about :facebook :picture]           
           
           []                              [:messages] 
           []                              [:scores] 
           
           ))))


