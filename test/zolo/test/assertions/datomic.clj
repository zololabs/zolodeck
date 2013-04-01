(ns zolo.test.assertions.datomic
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.test.assertions.core
        [clojure.test :only [is are]])
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.messages :as fb-messages]
            [zolo.utils.calendar :as zolo-cal]
            [zolodeck.demonic.core :as demonic]))


(defn has-datomic-id? [entity]
  (number? (:db/id entity)))

(defn assert-datomic-id-present [entity]
  (is (has-datomic-id? entity)))

(defn assert-datomic-id-not-present [entity]
  (is (not (has-datomic-id? entity))))

(defn- datomic-entity-count [a n]
  (count (demonic/run-query '[:find ?e :in $ ?a :where [?e ?a _]] a)))

(defn assert-datomic-user-count [n]
  (is (= n (datomic-entity-count :user/guid n))))

(defn assert-datomic-user-identity-count [n]
  (is (= n (datomic-entity-count :identity/guid n))))

(defn assert-datomic-contact-count [n]
  (is (= n (datomic-entity-count :contact/guid n))))

(defn assert-datomic-social-count [n]
  (is (= n (datomic-entity-count :social/guid n))))

(defn assert-datomic-message-count [n]
  (is (= n (datomic-entity-count :message/guid n))))

(defn assert-datomic-temp-message-count [n]
  (is (= n (datomic-entity-count :temp-message/guid n))))

(defn assert-datomic-suggestion-set-count [n]
  (is (= n (datomic-entity-count :suggestion-set/guid n))))

