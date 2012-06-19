(ns zolo.domain.message-test
  (:use zolodeck.demonic.test
        zolo.test.core-utils
        zolodeck.utils.debug
        zolodeck.utils.test
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]
            [zolo.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
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
         (is (= 3 (count messages-with-jill)))))))


  ;; (demonic-testing "When Contact is not Present"
  ;;   (fb/in-facebook-lab
  ;;    (let [amit (fb/create-user "Amit" "Rathore")
  ;;          deepthi (fb/create-user "Deepthi" "Somasunder")]
  ;;      (user/insert-fb-user amit)

  ;;      (fb/make-friend amit deepthi)

  ;;      (is (empty? (:user/messages (user/find-by-fb-id (:id amit)))))
  ;;      (is (empty? (:user/contacts (user/find-by-fb-id (:id amit)))))
       
  ;;      (fb/send-message amit deepthi "1" "Hi, what's going on?" "2012-05-01")
  ;;      (fb/send-message deepthi amit "1" "Nothing, just work..." "2012-05-02")
  ;;      (fb/send-message amit deepthi "1" "OK, should I get groceries?" "2012-05-03")

  ;;      (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages amit)]
  ;;        (user/update-facebook-inbox (:id amit))
  ;;        (is (= 3 (count (:user/messages (user/find-by-fb-id (:id amit))))))
  ;;        (is (= 1 (count (:user/contacts (user/find-by-fb-id (:id amit)))))))
  ;;      )))


  )

;; (deftest test-fb-message->zolo-message

;;   (testing "when nil is passed")
  
;;   (demonic-testing "when valid message is passed"
;;     (let [jack-msg (->> (vincent/create)
;;                     :user/messages
;;                     (sort-by :message/text)
;;                     first
;;                     print-vals)]

;;       (are [expected key-seq] (= expected (get-in jack-msg key-seq))

;;            (jack :contact/guid)            [:zolo-id]
;;            (jack :contact/first-name)      [:about :first-name]
;;            (jack :contact/last-name)       [:about :last-name]
;;            (jack :contact/gender)          [:about :gender]
;;            (jack :contact/fb-id)           [:about :facebook :id]           
;;            (jack :contact/fb-link)         [:about :facebook :link]           
;;            (jack :contact/fb-birthday)     [:about :facebook :birthday]           
;;            (jack :contact/fb-picture-link) [:about :facebook :picture]           
           
;;            []                              [:messages] 
;;            []                              [:scores] 
           
;;            )
;;       )

;;     ))


