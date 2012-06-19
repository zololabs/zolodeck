(ns zolo.domain.message-test
  (:use zolodeck.demonic.test
        zolo.test.core-utils
        zolodeck.utils.debug
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

(deftest test-update-inbox
  (demonic-testing "When Contact is already Present"
    (fb/in-facebook-lab
     (let [amit (fb/create-user "Amit" "Rathore")
           deepthi (fb/create-user "Deepthi" "Somasunder")
           siva (fb/create-user "Siva" "Jagadeesan")]
       (user/insert-fb-user amit)

       (fb/make-friend amit deepthi)
       (fb/make-friend amit siva)

       (personas/update-fb-friends amit)

       (is (empty? (:user/messages (user/find-by-fb-id (:id amit)))))
       (is (not (empty? (:user/contacts (user/find-by-fb-id (:id amit))))))
       
       (fb/send-message amit deepthi "1" "Hi, what's going on?" "2012-05-01")
       (fb/send-message deepthi amit "1" "Nothing, just work..." "2012-05-02")
       (fb/send-message amit deepthi "1" "OK, should I get groceries?" "2012-05-03")

       (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages amit)]
         (user/update-facebook-inbox (:id amit))
         (is (= 3 (count (:user/messages (user/find-by-fb-id (:id amit))))))
         (is (not (empty? (:user/contacts (user/find-by-fb-id (:id amit)))))))
       
       (fb/send-message amit siva "2" "Hi, how's  it going?" "2012-06-01")
       (fb/send-message siva amit "2" "Good, I finished writing the tests" "2012-06-02")
       (fb/send-message amit siva "2" "OK, did you update the card?" "2012-06-03")

       (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages amit)]
         (user/update-facebook-inbox (:id amit))
         (is (= 6 (count (:user/messages (user/find-by-fb-id (:id amit)))))))))))

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


