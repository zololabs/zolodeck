(ns zolo.domain.message-test
  (:use zolodeck.demonic.test
        zolo.test.core-utils
        zolo.utils.debug
        zolo.utils.test
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolo.facebook.gateway :as fb-gateway]
            [zolo.marconi.facebook.factory :as fb-factory]
            [zolo.marconi.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.loner :as loner]
            [zolo.personas.core :as personas]
            [zolo.facebook.inbox :as fb-inbox]))

(deftest test-group-by-contact
  (let [u1 {:user/fb-id "u1"}
        m1 {:message/from "c1"
            :message/to "u1"
            :messages/guid "1"}
        m2 {:message/from "c2"
            :message/to "u1"
            :messages/guid "2"}
        m3 {:message/from "u1"
            :message/to "c2"
            :messages/guid "3"}
        m [m1 m2 m3]
        grouped (message/group-by-contact-fb-id u1 m)]
    (is-same-sequence? ["c1" "c2"] (keys grouped))
    (is-same-sequence? [m1] (grouped "c1"))
    (is-same-sequence? [m2 m3] (grouped "c2"))
    ))

(deftest test-update-inbox
  
  (demonic-testing "When Contact is already Present"
    (fb/in-facebook-lab
     (let [vincent (vincent/create)
           jack (personas/friend-of vincent "jack")
           jill (personas/friend-of vincent "jill")]

       (is (= 2  (count (:user/contacts vincent))))

       (let [messages-with-jack (:contact/messages jack)
             messages-with-jill (:contact/messages jill)]

         (is (= 3 (count messages-with-jack)))
         (is (= 2 (count messages-with-jill)))))))


  (demonic-testing "When Contact is not Present"
    (fb/in-facebook-lab
     (let [loner (personas/empty-fb-user "Loner" "Hal")
           god (personas/empty-fb-user "Iam" "God")]

       (fb/make-friend loner god)

       (fb/send-message loner god "1" "Hi, what's going on?" "2012-05-01")
       (fb/send-message god loner "1" "Nothing, just work..." "2012-05-02")
       (fb/send-message loner god "1" "OK, should I get groceries?" "2012-05-03")

       (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages loner)]
         (user/update-facebook-inbox (:id loner))
         (is (= 1 (count (:user/contacts (user/find-by-fb-id (:id loner)))))))))))




