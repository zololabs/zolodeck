(ns zolo.domain.zolo-graph-test
  (:use zolodeck.utils.debug
        zolodeck.demonic.test
        [zolo.domain.zolo-graph :as zg]
        [clojure.test :only [run-tests deftest is are testing]]
        zolo.test.assertions)
  (:require [zolo.factories.zolo-graph-factory :as zgf]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.personas.vincent :as vincent]
            [zolo.personas.loner :as loner]
            [zolo.personas.core :as personas]
            [zolo.personas.shy :as shy]
            [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.message :as message]))

(def contact1 (zgf/new-contact #G"cccc1000"))
(def contact2 (zgf/new-contact #G"cccc2000"))

(deftest test-user-guid
  (is (= #G"aaaa1000" (user-guid (zgf/new-user #G"aaaa1000")))))

(deftest test-contact-guid
  (is (= #G"cccc1000" (user-guid (zgf/new-contact #G"cccc1000")))))

(deftest test-contact-guids
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/add-contact contact2))]
    (is (= #{#G"cccc1000" #G"cccc2000"} (set (contact-guids zg))))))

(deftest test-contacts
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/add-contact contact2))]
    (is (= 2 (count (contacts zg))))
    (is (= #G"cccc2000" (:guid (contact zg #G"cccc2000"))))))

(deftest test-messages
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/send-message contact1 "send 1")
            (zgf/receive-message contact1 "recieve 1")
            (zgf/add-contact contact2)
            (zgf/send-message contact2 "send 2")
            (zgf/receive-message contact2 "recieve 2"))]
    (is (= #{"send 2" "recieve 2"} 
           (set (map :text (messages zg #G"cccc2000")))))
    (is (= #{"send 1" "send 2" "recieve 1" "recieve 2"} 
           (set (map :text (all-messages zg)))))))


(deftest test-score
  (testing "When score present"
    (testing "it should return latest score"
      (let [zg (zgf/building 
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1)
                (zgf/add-score contact1 100 #inst "1980-08-08T00:00:00.000-00:00")
                (zgf/add-score contact1 101 #inst "1990-08-08T00:00:00.000-00:00")
                (zgf/add-score contact1 200 #inst "2000-08-08T00:00:00.000-00:00"))]
        (is (= true (zg/has-score? zg #G"cccc1000")))
        (is (= {:value 200 :at #inst "2000-08-08T00:00:00.000-00:00"} (zg/score zg #G"cccc1000")))
        (is (= 200 (zg/score-value (zg/score zg #G"cccc1000")))))))
  
  (testing "when no score is present"
    (testing "it should return nil and -1 for value"
      (let [zg (zgf/building 
                (zgf/new-user #G"aaaa1000")
                (zgf/add-contact contact1))]
        (is (= false (zg/has-score? zg #G"cccc1000")))
        (is (nil? (zg/score zg #G"cccc1000")))
        (is (= -1 (zg/score-value (zg/score zg #G"cccc1000"))))))))

(deftest test-scores
  (let [zg (zgf/building 
            (zgf/new-user #G"aaaa1000")
            (zgf/add-contact contact1)
            (zgf/add-score contact1 100)
            (zgf/add-score contact1 101)
            (zgf/add-contact contact2)
            (zgf/add-score contact2 200)
            (zgf/add-score contact2 201))]
    (is (= #{200 201} 
           (set (map :value (scores zg #G"cccc2000")))))
    (is (= #{100 101 200 201} 
           (set (map :value (all-scores zg)))))))


;; Tests for Constructions

(deftest test-score->zolo-score

  (testing "when nil is passed"
    (is (nil? (zg/score->zg-score nil))))
  
  (demonic-testing "when valid score is passed"
    (let [jack-score (-> (vincent/create-with-score)
                         (personas/friend-of "jack")
                         :contact/scores
                         first)
          jack-zg-score (zg/score->zg-score jack-score)]

      (are [expected key-seq] (= expected (get-in jack-zg-score key-seq))

           (jack-score :score/guid)         [:guid]
           (jack-score :score/value)        [:value]
           (jack-score :score/at)           [:at]))))

(deftest test-message->zolo-message

  (testing "when nil is passed"
    (is (nil? (zg/message->zg-message nil))))
  
  (demonic-testing "when valid message is passed"
    (let [jack-msg (-> (vincent/create)
                       (personas/friend-of "jack")
                       :contact/messages
                       first)
          jack-zg-msg (zg/message->zg-message jack-msg)]

      (are [expected key-seq] (= expected (get-in jack-zg-msg key-seq))

           (jack-msg :message/guid)            [:guid]
           (jack-msg :message/message-id)      [:message-id]
           (jack-msg :message/platform)        [:platform]
           (jack-msg :message/mode)            [:mode]
           (jack-msg :message/text)            [:text]
           (jack-msg :message/date)            [:date]
           (jack-msg :message/from)            [:from]
           (jack-msg :message/to)              [:to]
           (jack-msg :message/thread-id)       [:thread-id]
           (jack-msg :message/reply-to)        [:reply-to]
           ))))


(deftest test-contact->zolo-contact

  (testing "when nil is passed"
    (is (nil? (zg/contact->zolo-contact-score nil)))

  (demonic-testing "when valid contact is passed"
    (let [vincent (vincent/create-with-score)
          jack (personas/friend-of vincent "jack")
          jack-zg (zg/contact->zolo-contact jack)]

      (are [expected key-seq] (= expected (get-in jack-zg key-seq))

           (jack :contact/guid)            [:guid]
           (jack :contact/first-name)      [:about :first-name]
           (jack :contact/last-name)       [:about :last-name]
           (jack :contact/gender)          [:about :gender]
           (jack :contact/fb-id)           [:about :facebook :id]           
           (jack :contact/fb-link)         [:about :facebook :link]           
           (jack :contact/fb-birthday)     [:about :facebook :birthday]           
           (jack :contact/fb-picture-link) [:about :facebook :picture])

      (is (= 3 (count (:messages jack-zg))))
      (is (= 1 (count (:scores jack-zg)))))))

;;TODO Need to finish all these test scenarios
(deftest test-user->zolo-graph

  (testing "When nil is passed"
    (assert-zg-is-not-valid (zg/user->zolo-graph nil)))
  
  (demonic-testing "User without any contacts"
    (let [zg (-> (loner/create)
                 zg/user->zolo-graph)]
      (assert-zg-is-valid zg)
      (assert-zg-has-no-contacts zg)))
  
  (testing "User with contacts"
    (testing "and has NO messages"
      (demonic-testing "and has scores")
      (demonic-testing "but NO scores"
        (let [zg (-> (shy/create)
                     zg/user->zolo-graph)]
          (assert-zg-is-valid zg)
          (assert-zg-has-contacts zg 2)))))

    (testing "and has messages"
      (demonic-testing "and has scores")
      (demonic-testing "but NO scores"
        (let [vincent (vincent/create)
              jack (personas/friend-of vincent "jack")
              jill (personas/friend-of vincent "jill")
              zg (zg/user->zolo-graph vincent)]
          (assert-zg-is-valid zg)
          (assert-zg-has-contacts zg 2)
          (assert-zg-contact-has-messages zg jack 3)
          (assert-zg-contact-has-messages zg jill 2)))))