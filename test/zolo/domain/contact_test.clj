(ns zolo.domain.contact-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.domain.user :as user]
            [zolo.social.core :as social]
            [zolo.domain.core :as d-core]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.domain.contact :as contact]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.social-identity :as si]
            [zolo.domain.message :as message]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.personas.shy :as shy-persona]
            [zolo.personas.generator :as pgen]))

(defn- fetch-social-identities [fb-user]
  (social/fetch-social-identities :provider/facebook "at" (:id fb-user) "date"))

(deftest test-updated-contacts
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         jack (fb-lab/create-friend "Jack" "Daniels")
         jill (fb-lab/create-friend  "Jill" "Ferry")
         mary (fb-lab/create-friend  "Mary" "Jane")
         d-mickey (personas/create-domain-user-from-fb-user mickey)]

     (fb-lab/make-friend mickey jack)
     (fb-lab/make-friend mickey jill)

     (let [d-m-contacts (contact/updated-contacts (:user/contacts d-mickey)
                                                  (fetch-social-identities mickey))]

       (testing "Contacts are nil should create new contacts with new SIs"
         (is (= 2 (count d-m-contacts)))
         (d-assert/contacts-list-are-same [jack jill]
                                          (sort-by contact/first-name d-m-contacts)))
       
       
       (testing "sis is nil or empty should return no unchanged contacts"

         (let [d2-m-contacts (contact/updated-contacts d-m-contacts
                                                       [])]
           (is (= (sort-by #(-> % :contact/social-identities first :social/first-name) d-m-contacts)
                  (sort-by #(-> % :contact/social-identities first :social/first-name) d2-m-contacts)))))
       
       (testing "Contact not present with given si should return append a new contact"

          (fb-lab/make-friend mickey mary)
          (let [d3-m-contacts (contact/updated-contacts d-m-contacts
                                                        (fetch-social-identities mickey))]
            
            (is (= 3 (count d3-m-contacts)))
            (d-assert/contacts-list-are-same [jack jill mary]
                                             (sort-by contact/first-name d3-m-contacts)))
          (fb-lab/unfriend mickey mary))

       (testing "Contact present with given si should return update contact's si"

         (let [updated-jill (fb-lab/update-friend (:id jill)
                                                  {:last_name "Jackson" :name "Jill Jackson"})
               d4-m-contacts (contact/updated-contacts d-m-contacts
                                                       (fetch-social-identities mickey))]
            (is (= 2 (count d4-m-contacts)))
            (d-assert/contacts-list-are-same [jack updated-jill]
                                             (sort-by contact/first-name d4-m-contacts))))))))

(deftest test-provider-id
  (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})]

      (let [[jack] (sort-by contact/first-name (:user/contacts u))]

          (testing "When incorrect provider is passed it should throw exception"            
            (is (thrown-with-msg? RuntimeException #"Unknown provider specified: :provider/junk"
                  (contact/provider-id jack :provider/junk))))

          (testing "When profile with provider is present it should return correct value"
            (is (not (nil? (contact/provider-id jack :provider/facebook))))
            (is (= (si/fb-id jack) (contact/provider-id jack :provider/facebook)))))))

(deftest test-days-not-contacted
  (testing "when never contacted it should return -1"
    (let [shy (shy-persona/create-domain)
          ibc (interaction/ibc shy (:user/contacts shy))]

      (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))]
        (is (= -1 (contact/days-not-contacted jack ibc)))
        (is (= -1 (contact/days-not-contacted jill ibc))))))

  (testing "when contacted today it should return 0"
    (run-as-of "2012-05-10"
      (let [u (pgen/generate-domain {:SPECS { :friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
            ibc (interaction/ibc u (:user/contacts u))]

        (is (= 0 (contact/days-not-contacted (first (:user/contacts u)) ibc))))))

  (testing "when contacted some day ago it should return proper dates"
    (let [u (pgen/generate-domain {:SPECS { :friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
          ibc (interaction/ibc u (:user/contacts u))]

      (are [expected as-of-date] (= expected (run-as-of as-of-date
                                               (contact/days-not-contacted (first (:user/contacts u)) ibc)))
           0   "2012-05-10"
           10   "2012-05-20"
           31   "2012-06-10"
           41   "2012-06-20"))))

(deftest test-is-contacted-today
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                                    (pgen/create-friend-spec "Jill" "Ferry" 3 6)]}})
         ibc (interaction/ibc u (:user/contacts u))]
     
     (let [[jack jill] (sort-by contact/first-name (:user/contacts u))]
       (testing "When not contacted"
         (run-as-of "2012-05-09"
           (is (not (contact/is-contacted-today? jack ibc)))
           (is (not (contact/is-contacted-today? jill ibc)))))

       (testing "When contacted"
         (run-as-of "2012-05-12"
           (is (not (contact/is-contacted-today? jack ibc)))
           (is (contact/is-contacted-today? jill ibc))))))))

(deftest test-contacts-not-contacted-for-days
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 0 0)
                                                    (pgen/create-friend-spec "Jill" "Ferry" 3 6)]}})
         ibc (interaction/ibc u (:user/contacts u))]
     
     (let [[jack jill] (sort-by contact/first-name (:user/contacts u))]
       (run-as-of "2012-05-22"
         (is (= 2 (count (contact/contacts-not-contacted-for-days ibc 1))))
         (is (= #{jack jill} (set (contact/contacts-not-contacted-for-days ibc 1)))))

       (run-as-of "2012-05-12"
         (is (= 1 (count (contact/contacts-not-contacted-for-days ibc 1))))
         (is (= #{jack} (set (contact/contacts-not-contacted-for-days ibc 1)))))))))

(deftest test-contact-score
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 3 6)]}})
         jack (first (:user/contacts u))]

     (testing "When score is not present it should return 0"
       (is (= 0 (contact/contact-score (dissoc jack :contact/score)))))

     (testing "When score is present it should return score"
       (is (= 30 (contact/contact-score jack)))))))

(deftest test-is-muted
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
         jack (first (:user/contacts u))]

     (testing "When :contact/muted is nil it should return false"
       (is (not (contact/is-muted? jack))))

     (testing "When :contact/muted is false it should return false"
       (is (not (contact/is-muted? (assoc jack :contact/muted false)))))

     (testing "When :contact/muted is true it should return true"
       (is (contact/is-muted? (assoc jack :contact/muted true)))))))

(deftest test-is-a-person
  (d-core/run-in-gmt-tz
   (pgen/run-demarcated-generative-tests u {:SPECS {:friends [(pgen/create-friend-spec "admin" "thoughtworks" 1 1)
                                                              (pgen/create-friend-spec "jack" "ripper" 1 1)]}
                                            :UI-IDS-ALLOWED [:EMAIL :FACEBOOK]}
      (let [admin (->> u :user/contacts (sort-by #(-> % :contact/social-identities first :social/first-name)) first)
            admin-si (-> admin :contact/social-identities first)]
        (testing "When :contact/is-a-person is nil, and is provider/email it depends on the email address"
          (if (si/is-email? admin-si)
            (is (not (contact/is-a-person? admin)))
            (is (contact/is-a-person? admin))))
        
        (testing "When :contact/is-a-person is false it should return false"
          (is (not (contact/is-a-person? (assoc admin :contact/is-a-person false)))))
        
        (testing "When :contact/is-a-person is true it should return true"
          (is (contact/is-a-person? (assoc admin :contact/is-a-person true))))))))

(deftest test-contacts-with-score
  (d-core/run-in-gmt-tz
   (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Strong" "Contact" 50 50)
                                                    (pgen/create-friend-spec "Medium" "Contact" 10 10)
                                                    (pgen/create-friend-spec "Weak1" "Contact1" 5 5)
                                                    (pgen/create-friend-spec "Weak2" "Contact2" 0 0)]}})
         [medium strong weak1 weak2] (sort-by contact/first-name (:user/contacts u))]

     (is (= 1 (count (contact/strong-contacts u))))
     (is (= #{strong} (set (contact/strong-contacts u))))

     (is (= 1 (count (contact/medium-contacts u))))
     (is (= #{medium} (set (contact/medium-contacts u))))

     (is (= 2 (count (contact/weak-contacts u))))
     (is (= #{weak1 weak2} (set (contact/weak-contacts u))))
     )))

;; (demonictest test-contact-list
;;   (personas/in-social-lab
;;      (let [mickey (fb-lab/create-user "Mickey" "Mouse")
;;            db-mickey (user/signup-new-user (personas/create-social-user mickey))
;;            strong-friends (fb-lab/create-friends "strong" 10)
;;            medium-friends (fb-lab/create-friends "medium" 20)
;;            weak-friends (fb-lab/create-friends "weak" 30)
;;            all-friends (concat strong-friends medium-friends weak-friends)]

;;        (doseq [f all-friends]
;;          (fb-lab/make-friend mickey f))
       
;;        (fb-lab/login-as mickey)
       
;;        (contact/update-contacts (user/reload db-mickey))

;;        (stubbing [contact/contact-score (fn [c]
;;                                           (let [fname (:contact/first-name c)]
;;                                             (cond 
;;                                              (.contains fname "strong") 500
;;                                              (.contains fname "medium") 200
;;                                              (.contains fname "weak") 50
;;                                              :else 0)))]
         
;;          (testing "No Selectors should return all contacts"
;;            (is (= 60 (count (contact/list-contacts (user/reload db-mickey) {})))))

;;          (testing "Selectors should return only Selected contacts"
;;            (is (= 10 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong]}))))
;;            (assert-all-contacts-are :strong (contact/list-contacts (user/reload db-mickey) {:selectors [:strong]}))
           
;;            (is (= 20 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:medium]}))))
;;            (assert-all-contacts-are :medium (contact/list-contacts (user/reload db-mickey) {:selectors [:medium]}))
           
;;            (is (= 30 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:weak]}))))
;;            (assert-all-contacts-are :weak (contact/list-contacts (user/reload db-mickey) {:selectors [:weak]}))
           
;;            (is (= 30 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :medium]}))))
;;            (assert-no-contacts-are :weak (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :medium]}))

;;            (is (= 40 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :weak]}))))
;;            (assert-no-contacts-are :medium (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :weak]}))

;;            (is (= 10 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong :strong]})))))


;;          (testing "Pagingation"
;;            (is (= 5 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong] :limit 5}))))
;;            (is (= 10 (count (contact/list-contacts (user/reload db-mickey) {:selectors [:strong] :limit 50}))))           
;;            (is (= 5 (count (contact/list-contacts (user/reload db-mickey) {:selectors [] :limit 50 :offset 55})))))))))


