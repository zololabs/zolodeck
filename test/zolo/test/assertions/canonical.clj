(ns zolo.test.assertions.canonical
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        zolo.test.assertions.core
        [clojure.test :only [is are]])
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.messages :as fb-messages]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.demonic.core :as demonic]
            [clj-time.coerce :as ctc]))

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

(defn assert-feed [fb-feed canonical-feed]
  (let [fb-feed-keys [:id :message :story :picture :link :icon]
        canonical-feed-keys [:message/message-id  :message/text :message/story :message/picture :message/link :message/icon]]
    (assert-map-values fb-feed fb-feed-keys canonical-feed canonical-feed-keys)
    (is (= :provider/facebook (:message/provider canonical-feed)))
    (is (= (-> fb-feed :from :id)  (:message/from canonical-feed)))
    (is (= (->> fb-feed :to :data (map :id))  (:message/to canonical-feed)))
    ;;TODO Need to do this check
    ;;(is (= (ctc/to-date (:created_time fb-feed)) (:message/date canonical-feed)))
    ))

