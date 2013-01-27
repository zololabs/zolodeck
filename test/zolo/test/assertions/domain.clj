(ns zolo.test.assertions.domain
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure
        zolo.test.assertions.core
        [clojure.test :only [is are]])
  (:require [zolo.social.core :as social]
            [zolo.social.facebook.messages :as fb-messages]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.demonic.core :as demonic]))

(defn social-identities-are-same [fb-contact db-si]
  (assert-map-values fb-contact [:username :link :id :last_name :first_name]
                     db-si [:social/nickname :social/profile-url :social/provider-uid :social/last-name :social/first-name])

  (is (= (:birthday fb-contact) (str (:social/birth-month db-si) "/"
                                     (:social/birth-day db-si) "/"
                                     (:social/birth-year db-si))))
  (is (= (social/gender-enum (:gender fb-contact)) (:social/gender db-si))))

(defn contacts-are-same [fb-contact db-contact]
  (assert-map-values fb-contact [:first_name :last_name]
                     db-contact [:contact/first-name :contact/last-name])

  (social-identities-are-same fb-contact (first (:contact/social-identities db-contact))))

(defn contact-is-muted [db-contact]
  (is (:contact/muted db-contact) (str (:contact/first-name db-contact) " is not muted!")))

(defn contact-is-not-muted [db-contact]
  (is (not (:contact/muted db-contact)) (str (:contact/first-name db-contact) " is muted!")))