(ns zolo.service.distiller.contact-test
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
            [zolo.personas.generator :as pgen]
            [zolo.service.distiller.contact :as c-distiller]
            [zolo.service.distiller.social-identity :as si-distiller]))

(deftest test-distill
  (testing "When nil is passed it should return nil"
    (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)]}})
         ibc (interaction/ibc u (:user/contacts u))]
      (is (nil? (c-distiller/distill nil ibc)))))

  (testing "When proper contact without interactions is passed"
    (d-core/run-in-gmt-tz
     (let [shy (shy-persona/create-domain)
           ibc (interaction/ibc shy (:user/contacts shy))]

       (let [[jack jill] (sort-by contact/first-name (:user/contacts shy))
             distilled-jack (c-distiller/distill jack ibc)]
         (is (= "Jack" (:contact/first-name distilled-jack)))
         (is (= "Daniels" (:contact/last-name distilled-jack)))
         (is (= (:contact/guid jack) (:contact/guid distilled-jack)))
         (is (= (contact/picture-url jack) (:contact/picture-url distilled-jack)))
         (is (not (:contacted-today distilled-jack)))
         (is (not (:contact/muted distilled-jack)))
         (is (:contact/person distilled-jack))
         (is (empty? (:contact/interaction-daily-counts distilled-jack)))))))

  (testing "When proper contact with interactions is passed"
    (run-as-of "2012-05-10"
      (d-core/run-in-gmt-tz
       (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 2)
                                                        ;(pgen/create-friend-spec
                                                        ;"Jill"
                                                        ;"Ferry" 3 3)
                                                        ]}})
             ibc (interaction/ibc u (:user/contacts u))]
         
         (let [[jack jill] (sort-by contact/first-name (:user/contacts u))
               distilled-jack (c-distiller/distill jack ibc)]
           (is (= "Jack" (:contact/first-name distilled-jack)))
           (is (= "Daniels" (:contact/last-name distilled-jack)))
           (is (= (:contact/guid jack) (:contact/guid distilled-jack)))
           (is (= (contact/picture-url jack) (:contact/picture-url distilled-jack)))
           (is (:contacted-today distilled-jack))
           (is (not (:contact/muted distilled-jack)))
           (is (= [["2012-05-10" 1]] (:contact/interaction-daily-counts distilled-jack))))))))


  (testing "Social Idenitity should be also there as part of distilled user"
    (run-as-of "2012-05-10"
      (d-core/run-in-gmt-tz
       (let [u (pgen/generate-domain {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 2)]}})
             ibc (interaction/ibc u (:user/contacts u))]
         
         (let [jack (first (:user/contacts u))
               distilled-jack (c-distiller/distill jack ibc)]

           (is (= 1 (count (:contact/social-identities distilled-jack))))
           (is (= [(si-distiller/distill (first (:contact/social-identities jack)))]
                  (:contact/social-identities distilled-jack)))))))))

