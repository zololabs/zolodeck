(ns zolo.test.assertions.domain
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.test.assertions.core
        [clojure.test :only [is are]])
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.messages :as fb-messages]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.demonic.core :as demonic]))

(defn user-identities-are-same [fb-user db-ui]
  (assert-map-values fb-user [:uid :first_name :last_name :email :locale :pic_big :pic_small :profile_url :username]
                     db-ui [:identity/provider-uid :identity/first-name :identity/last-name :identity/email :identity/locale :identity/photo-url :identity/thumbnail-url :identity/profile-url :identity/nickname]))

(defn users-are-same [fb-user db-user]
  (assert-map-values fb-user [:first_name :last_name :uid]
                     db-user [:user/first-name :user/last-name :user/login-provider-uid])

  (user-identities-are-same fb-user (first (:user/user-identities db-user))))

(defn social-identities-are-same [fb-contact db-si]
  (assert-map-values fb-contact [:username :link :id :last_name :first_name]
                     db-si [:social/nickname :social/profile-url :social/provider-uid :social/last-name :social/first-name])

  (is (= (:birthday fb-contact) (str (:social/birth-month db-si) "/"
                                     (:social/birth-day db-si) "/"
                                     (:social/birth-year db-si))))
  (is (= (social/gender-enum (:gender fb-contact)) (:social/gender db-si))))

(defn contacts-are-same [fb-contact db-contact]
  ;;TODO Need to filter to get fb-si instead of first
  (let [db-fb-si (first (:contact/social-identities db-contact))]
    (is (not (nil? db-fb-si)) (str "Social Identity for " (:first_name fb-contact) " should not be  nil!!!"))
    (when db-fb-si
      (social-identities-are-same fb-contact db-fb-si))))

(defn contacts-list-are-same [fb-contacts db-contacts]
  (doall (map #(contacts-are-same %1 %2) fb-contacts db-contacts)))

(defn contact-is-muted [db-contact]
  (is (:contact/muted db-contact) (str (:contact/first-name db-contact) " is not muted!")))

(defn contact-is-not-muted [db-contact]
  (is (not (:contact/muted db-contact)) (str (:contact/first-name db-contact) " is muted!")))

(defn messages-are-same [fb-message db-message]
  (let [fb-message-keys [:author_id :body :message_id :thread_id :to]
        db-message-keys [:message/from :message/text :message/message-id :message/thread-id :message/to]]
    (assert-map-values fb-message fb-message-keys db-message db-message-keys)
    (is (= :provider/facebook (:message/provider db-message)))
    (is (= (zolo-cal/millis->instant (-> fb-message :created_time (* 1000))) (:message/date db-message)))
    (if (empty? (:attachment fb-message))
      (is (nil? (:message/attachments db-message)))
      (is (same-value? (:attachment fb-message) (:message/attachments db-message))))))

(defn messages-list-are-same [fb-messages db-messages]
  (doall (map #(messages-are-same %1 %2) fb-messages db-messages)))

(defn temp-messages-are-same [t-message db-t-message]

  (let [keys [:temp-message/provider :temp-message/from :temp-message/text :temp-message/date]]
    (assert-map-values t-message keys db-t-message keys))

  (is (= #{(:temp-message/to t-message)}
         (:temp-message/to db-t-message))))