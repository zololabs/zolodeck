(ns zolo.domain.suggestion-set-test
  (:use zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.core :as d-core]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.suggestion-set :as ss]
            [zolo.personas.factory :as personas]
            [zolo.personas.generator :as pgen]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.utils.calendar :as zolo-cal]))

(defn- create-ss [ss-name contacts]
  {:suggestion-set/name ss-name
   :suggestion-set/contacts contacts})

(deftest test-suggestion-set-name
  (testing "when bad data is passed it should throw exception"
    (is (thrown? RuntimeException (ss/suggestion-set-name nil)))
    (is (thrown? RuntimeException (ss/suggestion-set-name "JUNK"))))
  
  (testing "when an instant is passed it should return name"
    (is (= "ss-2012-12-21" (ss/suggestion-set-name (zolo-cal/date-string->instant "yyyy-MM-dd" "2012-12-21"))))))

(deftest test-suggestion-set
  (personas/in-social-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         d-mickey (-> (personas/create-domain-user mickey)
                      (assoc :user/suggestion-sets [(create-ss "ss-2012-12-21" ["c1" "c2"])
                                                    (create-ss "ss-2012-11-11" ["c3" "c4"])]))]

     (testing "when suggestion set is not present it should return nil"
       (is (nil? (ss/suggestion-set d-mickey "notpresent"))))
     
     (testing "when suggestion set is present it should return suggestion-set"
       (is (not (nil? (ss/suggestion-set d-mickey "ss-2012-12-21"))))
       (is (= ["c1" "c2"] (:suggestion-set/contacts (ss/suggestion-set d-mickey "ss-2012-12-21"))))))))

(deftest test-distill
  (testing "When nil ss is passed it should return nil"
    (is (nil? (ss/distill nil nil))))

  (testing "When proper ss is passed and"

    (run-as-of "2012-05-11"
      (d-core/run-in-gmt-tz

       (testing "Contact is empty it should return proper ss name"
         (let [u (pgen/generate-domain {:SPECS {:friends []}})
               ibc (interaction/ibc u)
               ss (create-ss "ss-2012-05-01" (:user/contacts u))
               distilled-ss (ss/distill ss ibc)]
           (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
           (is (= [] (:suggestion-set/contacts distilled-ss)))))

       (testing "has many contacts"
         (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels")
                                                             (pgen/create-friend-spec "Jill" "Ferry")]}})
               ibc (interaction/ibc u)
               ss (create-ss "ss-2012-05-01" (:user/contacts u))
               distilled-ss (ss/distill ss ibc)]

           (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
           (is (= 2 (count (:suggestion-set/contacts distilled-ss))))))

       (testing "Contact has not been contacted at all"
         (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels")]}})
               ibc (interaction/ibc u)
               ss (create-ss "ss-2012-05-01" (:user/contacts u))
               distilled-ss (ss/distill ss ibc)]
           (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
           (is (= 1 (count (:suggestion-set/contacts distilled-ss))))

           (let [jack (first (:user/contacts u))
                 jack-from-ss (first (:suggestion-set/contacts distilled-ss))]
             (is (= (contact/distill jack ibc) (dissoc jack-from-ss :contact/reason-to-connect)))
             (is (= "You never interacted" (:contact/reason-to-connect jack-from-ss))))))

       (testing "Contact has been contacted last month"
         (run-as-of "2012-06-10"
           (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
                 ibc (interaction/ibc u)
                 ss (create-ss "ss-2012-05-01" (:user/contacts u))
                 distilled-ss (ss/distill ss ibc)]
             (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
             (is (= 1 (count (:suggestion-set/contacts distilled-ss))))

             (let [jack (first (:user/contacts u))
                   jack-from-ss (first (:suggestion-set/contacts distilled-ss))]
               (is (= (contact/distill jack ibc) (dissoc jack-from-ss :contact/reason-to-connect)))
               (is (= "Your last interaction was 31 days ago" (:contact/reason-to-connect jack-from-ss)))))))))))

(deftest test-new-suggestion-set
  (testing "When nil user is passed"
    (let [ss (ss/new-suggestion-set nil "ss-2012-12-01" :random)]
      (is (= "ss-2012-12-01" (:suggestion-set/name ss)))
      (is (empty? (:suggestion-set/contacts ss)))))

  (testing "when correct strategy is passed it should call the strategy"
    (let [startegy-fn (fn [u] (:user/contacts u))]
      (let [u (pgen/generate-domain {:SPECS {:friends (pgen/create-friend-specs 2)}})
            ss (ss/new-suggestion-set u "ss-2012-12-01" startegy-fn)]
        (is (= "ss-2012-12-01" (:suggestion-set/name ss)))
        (is (= 2 (count (:suggestion-set/contacts ss))))
        (is (= (sort-by contact/first-name (:user/contacts u))
               (sort-by contact/first-name (:suggestion-set/contacts ss))))))))