(ns zolo.service.distiller.user-test
  (:use [zolo.domain.user :as user]
        zolo.demonic.test
        zolo.demonic.core
        zolo.test.core-utils
        zolo.test.assertions.core
        zolo.utils.debug
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.service.distiller.user :as u-distiller]
            [zolo.domain.user :as user]
            [zolo.domain.user-identity :as ui]
            [zolo.domain.contact :as contact]
            [zolo.social.core :as social]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.utils.calendar :as zcal]
            [zolo.personas.generator :as pgen]))

(deftest test-distill
  (testing "Should return nil when Nil is passed"
    (is (nil? (u-distiller/distill nil))))
  
  (testing "Email should be empty if no user-identities are present"
    (let [du (u-distiller/distill {:user/guid "abc" :user/data-ready-in (zcal/now)})]
      (is (= "abc" (:user/guid du)))
      (is (empty? (:user/email du)))))

  (testing "Updated flag should be returned properly"
    (is (:user/updated (u-distiller/distill {:user/last-updated "sometime" :user/data-ready-in (zcal/now)})))
    (is (not (:user/updated (u-distiller/distill {:user/guid "abc" :user/data-ready-in (zcal/now)})))))
  
  (demonic-testing "Should return properly distilled user"
    (personas/in-social-lab
     (let [mickey (fb-lab/create-user "Mickey" "Mouse")
           d-mickey (personas/create-domain-user-from-fb-user mickey)
           du (u-distiller/distill d-mickey)]
       (is (= (str (:user/guid d-mickey)) (:user/guid du)))
       (is (= ["Mickey.Mouse@gmail.com"] (:user/emails du)))))))