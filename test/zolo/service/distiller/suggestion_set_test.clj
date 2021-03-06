(ns zolo.service.distiller.suggestion-set-test
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
            [zolo.utils.calendar :as zolo-cal]
            [zolo.service.distiller.contact :as c-distiller]
            [zolo.service.distiller.suggestion-set :as ss-distiller]))

(defn- create-ss [ss-name contacts]
  {:suggestion-set/name ss-name
   :suggestion-set/contacts contacts})

(deftest test-distill
  (testing "When nil ss is passed it should return nil"
    (is (nil? (ss-distiller/distill nil nil nil))))

  (testing "When proper ss is passed and"

    (run-as-of "2012-05-11"
      (d-core/run-in-gmt-tz

       (testing "Contact is empty it should return proper ss name"
         (let [u (pgen/generate-domain {:SPECS {:friends []}})
               ibc (interaction/ibc u (:user/contacts u))
               ss (create-ss "ss-2012-05-01" (:user/contacts u))
               distilled-ss (ss-distiller/distill ss u ibc)]
           (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
           (is (= [] (:suggestion-set/contacts distilled-ss)))))

       (testing "has many contacts"
         (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels")
                                                             (pgen/create-friend-spec "Jill" "Ferry")]}})
               ibc (interaction/ibc u (:user/contacts u))
               ss (create-ss "ss-2012-05-01" (:user/contacts u))
               distilled-ss (ss-distiller/distill ss u ibc)]

           (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
           (is (= 2 (count (:suggestion-set/contacts distilled-ss))))))

       (testing "Contact has not been contacted at all"
         (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels")]}})
               ibc (interaction/ibc u (:user/contacts u))
               ss (create-ss "ss-2012-05-01" (:user/contacts u))
               distilled-ss (ss-distiller/distill ss u ibc)]
           (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
           (is (= 1 (count (:suggestion-set/contacts distilled-ss))))

           (let [jack (first (:user/contacts u))
                 jack-from-ss (first (:suggestion-set/contacts distilled-ss))]
             (is (= (c-distiller/distill jack u ibc) (dissoc jack-from-ss :contact/reason-to-connect)))
             (is (= "You haven't connected in a while" (:contact/reason-to-connect jack-from-ss))))))

       (testing "Contact has been contacted last month"
         (run-as-of "2012-06-10"
           (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
                 ibc (interaction/ibc u (:user/contacts u))
                 ss (create-ss "ss-2012-05-01" (:user/contacts u))
                 distilled-ss (ss-distiller/distill ss u ibc)]
             (is (= "ss-2012-05-01" (:suggestion-set/name distilled-ss)))
             (is (= 1 (count (:suggestion-set/contacts distilled-ss))))

             (let [jack (first (:user/contacts u))
                   jack-from-ss (first (:suggestion-set/contacts distilled-ss))]
               (is (= (c-distiller/distill jack u ibc) (dissoc jack-from-ss :contact/reason-to-connect)))
               (is (= "Your last interaction was 31 days ago" (:contact/reason-to-connect jack-from-ss)))))))))))