(ns zolo.test.assertions
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        [clojure.test :only [is are]])
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.messages :as fb-messages]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.demonic.core :as demonic]))

(defn assert-map-values [m1 m1-keys m2 m2-keys]
  (is (= (count m1-keys) (count m2-keys)) "No of keys don't match")

  (doall (map #(is (not (nil? (m1 %))) (str % " shouldn't be nil in m1")) m1-keys))
  (doall (map #(is (not (nil? (m2 %))) (str % " shouldn't be nil in m2")) m2-keys))

  (doall (map #(is (= (%1 m1) (%2 m2)) (str %1 " does not match " %2)) m1-keys m2-keys)))

(defn assert-basic-user-info [fb-user canonical-user]
  (let [fb-user-keys [:first_name :last_name :uid]
        canonical-user-keys [:user/first-name :user/last-name :user/login-provider-uid]]
    (assert-map-values fb-user fb-user-keys canonical-user canonical-user-keys)))

(defn assert-user-identity [fb-user canonical-user-identity]
  (let [fb-user-keys [:uid :first_name :last_name :username :locale :email :pic_small :pic_big :profile_url]
        canonical-user-identity-keys [:identity/provider-uid :identity/first-name :identity/last-name :identity/nickname :identity/locale  :identity/email  :identity/thumbnail-url :identity/photo-url :identity/profile-url ]]
    (assert-map-values fb-user fb-user-keys canonical-user-identity canonical-user-identity-keys)
    (is (= (:birthday_date fb-user) (str (:identity/birth-month canonical-user-identity) "/"
                                         (:identity/birth-day canonical-user-identity) "/"
                                         (:identity/birth-year canonical-user-identity))))
    (is (= (social/gender-enum (:sex fb-user)) (:identity/gender canonical-user-identity)))
    (assert-map-values (:current_location fb-user) [:country :state :city :zip]
                       canonical-user-identity [:identity/country :identity/state :identity/city :identity/zip])))

(defn assert-social-identity [fb-contact canonical-social-identity]
  (let [fb-contact-keys [:last_name :first_name :link :username :id]
        canonical-si-keys [:social/last-name :social/first-name :social/profile-url :social/nickname :social/provider-uid]]
    (assert-map-values fb-contact fb-contact-keys canonical-social-identity canonical-si-keys)
    (is (= (:birthday fb-contact) (str (:social/birth-month canonical-social-identity) "/"
                                       (:social/birth-day canonical-social-identity) "/"
                                       (:social/birth-year canonical-social-identity))))

    (is (= (social/gender-enum (:gender fb-contact)) (:social/gender canonical-social-identity)))))

(defn assert-contact [fb-contact canonical-contact]
  (let [fb-contact-keys [:first_name :last_name]
        canonical-contact-keys [:contact/first-name :contact/last-name]]
    (assert-map-values fb-contact fb-contact-keys canonical-contact canonical-contact-keys)
    (assert-social-identity fb-contact (first (:contact/social-identities canonical-contact)))))

(defn assert-message [fb-message canonical-message]
  (let [fb-message-keys [:attachment :author_id :body :message_id :thread_id :to]
        canonical-message-keys [:message/attachments :message/from :message/text :message/message-id :message/thread-id :message/to]]
    (assert-map-values fb-message fb-message-keys canonical-message canonical-message-keys)
    (is (= :provider/facebook (:message/provider canonical-message)))
    (is (= (zolo-cal/millis->instant (-> fb-message :created_time (* 1000))) (:message/date canonical-message)))))

;; Domain Related Ones
(defn assert-contacts-are-same [expected-contact actual-contact]
  (is (= (set (keys expected-contact)) (set (keys actual-contact))))
  (map #(is (= (% expected-contact) (% actual-contact))) (keys expected-contact)))

;; Datomic related Ones
(defn has-datomic-id? [entity]
  (number? (:db/id entity)))

(defn assert-datomic-id-present [entity]
  (is (has-datomic-id? entity)))

(defn assert-datomic-id-not-present [entity]
  (is (not (has-datomic-id? entity))))

(defn datomic-entity-count [a n]
  (count (demonic/run-query '[:find ?e :in $ ?a :where [?e ?a _]] a)))

(defn assert-datomic-user-count [n]
  (is (= n (datomic-entity-count :user/guid n))))

(defn assert-datomic-user-identity-count [n]
  (is (= n (datomic-entity-count :identity/guid n))))

