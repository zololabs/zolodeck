(ns zolo.store.contact-store-test
  (:use clojure.test
        zolo.utils.debug
        zolo.utils.clojure
        zolo.demonic.test)
  (:require [zolo.store.contact-store :as c-store]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.personas.factory :as personas]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.personas.generator :as pgen]
            [zolo.domain.contact :as contact]))

(defn- remove-db-ids [c]
  (-> c
      (dissoc :db/id :contact/guid)
      (assoc :contact/social-identities (map #(dissoc % :db/id :social/guid)
                                             (:contact/social-identities c)))))

(demonictest test-find-by-guid
  (let [u (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                            (pgen/create-friend-spec "Jill" "Ferry" 1 1)]}})
        [jack jill] (sort-by contact/first-name (:user/contacts u))]
    
     (testing "when nil is passed it should return nil"
       (is (nil? (c-store/find-by-guid nil))))

     (testing "when random guid is passed it should return nil"
       (is (nil? (c-store/find-by-guid (random-guid-str)))))

     (testing "when contact is present it should return corrent contact"
       
       (let [db-jack (c-store/find-by-guid (:contact/guid jack))]
         (is (not (nil? db-jack)))
         (is (= jack db-jack))))))

(deftest test-save
  (let[u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
       jack (first (:user/contacts u))
       clean-jack (remove-db-ids jack)]

    (demonic-testing "new user saved"
      (db-assert/assert-datomic-contact-count 0)
      (db-assert/assert-datomic-social-count 0)
      
      (is (= clean-jack (remove-db-ids (c-store/save clean-jack))))

      (db-assert/assert-datomic-contact-count 1)
      (db-assert/assert-datomic-social-count 1))))

