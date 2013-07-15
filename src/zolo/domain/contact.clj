(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolo.utils.debug
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.string :as zolo-str]
            [zolo.utils.maps :as zmaps]
            [zolo.utils.calendar :as zcal]
            [zolo.utils.logger :as logger]
            [zolo.utils.domain :as utils-domain]
            [zolo.social.core :as social]
            [zolo.demonic.core :as demonic]
            [zolo.domain.social-identity :as si]
            [zolo.domain.score :as score]
            [zolo.domain.user :as user]
            [zolo.domain.interaction :as interaction]
            [zolo.domain.message :as message]
            [clojure.set :as set]
            [clj-time.core :as time])
  (:import org.joda.time.DateTime))

(defn- base-contact [si]
  {:contact/social-identities [si]})

(defn- value-from-si [c key]
  (-> c
      :contact/social-identities
      first
      key))

;;Public
(defn first-name [c]
  (value-from-si c :social/first-name))

(defn last-name [c]
  (value-from-si c :social/last-name))

(defn picture-url [c]
  (value-from-si c :social/photo-url))

(defn provider-id [c provider]
  (condp  = provider 
    :provider/facebook (si/fb-id c)
    (throw (RuntimeException. (str "Unknown provider specified: " provider)))))

(defn- contact-and-si-infos [contact]
  (->> contact
       :contact/social-identities
       (mapcat (fn [si] [(si/social-identity-info si) contact]))))

(defn- build-contacts-lookup-by-si [contacts]
  (->> contacts
       (mapcat contact-and-si-infos)
       (apply hash-map)))

(defn- contact-has-si? [c si]
  (some #(= (si/social-identity-info %)
            (si/social-identity-info si))
        (:contact/social-identities c)))

(defn- find-contact-with-si [cs si]
  (-> (filter #(contact-has-si? % si) cs)
      first))

(defn find-by-provider-and-provider-uid [user social-provider social-provider-uid]
  (find-contact-with-si (:user/contacts user)
                        {:social/provider social-provider :social/provider-uid social-provider-uid}))

(defn- update-si-in-contact [c fresh-si]
  (let [updated-si (-> (:contact/social-identities c)
                       (si/social-identity (si/social-identity-info fresh-si))
                       (merge fresh-si))]
    (zmaps/update-in-when c
                              [:contact/social-identities]
                              #(si/has-id? % (si/social-identity-info fresh-si))
                              updated-si)))

(defn- update-contacts-with-si [fresh-si contacts-by-si-info]
  (if-let [c (-> fresh-si si/social-identity-info contacts-by-si-info)]
    (assoc contacts-by-si-info (-> fresh-si si/social-identity-info) (update-si-in-contact c fresh-si))
    (assoc contacts-by-si-info (-> fresh-si si/social-identity-info) (base-contact fresh-si))))

(defn update-all-contacts [sis contacts-by-si-info]     
  (if (empty? sis)
    (vals contacts-by-si-info)
    (->> contacts-by-si-info
         (update-contacts-with-si (first sis))
         (recur (rest sis)))))

(defn updated-contacts [cs sis]
  (update-all-contacts sis (build-contacts-lookup-by-si cs)))

(defn days-not-contacted [c ibc]
  (let [interactions  (ibc c)]
    (if (empty? interactions)
      -1
      (let [ts (->> (interaction/messages-from-interactions interactions)                    
                    (keep message/message-date)
                    last
                    .getTime
                    DateTime.)
            n (time/now)
            i (time/interval ts n)]
        (time/in-days i)))))

(defn contacts-not-contacted-for-days [ibc days]
  (filter #(let [d (days-not-contacted % ibc)]
             (or (< d 0) (<= days d)))
          (keys ibc)))


;;TODO test
;;TODO need to move to store
(defn update-score [ibc c]
  (-> (assoc c :contact/score (score/calculate ibc c))
      demonic/insert))

(defn is-contacted-today? [c ibc]
  (let [last-send-message (->> ibc
                               (interaction/messages-from-ibc-for-contact c)
                               (message/last-sent-message c))]
    (zcal/same-day-instance? (zcal/now-joda (user/tz-offset-minutes))
                             (message/message-date last-send-message (user/tz-offset-minutes)))))

(defn is-muted? [c]
  (true? (:contact/muted c)))

;;TOOD test this
(defn is-a-person? [c]
  (when c
    (let [iap (:contact/is-a-person c)]
      (if-not (nil? iap)
        iap
        (every? #(si/is-a-person? %) (:contact/social-identities c))))))

(defn person-contacts [u]
  (->> u
       :user/contacts
       (filter is-a-person?)))

(defn contact-score [c]
  (or (:contact/score c) 0))

(defn- contacts-with-score-between [u lower upper]
  (->> (filter #(and (>= (contact-score %) lower) (<= (contact-score %) upper)) (person-contacts u))
       (sort-by :contact/score)
       reverse))

(defn strong-contacts
  ([u]
     (contacts-with-score-between u 301 1000000))
  ([u number]
     (take number (strong-contacts u))))

(defn medium-contacts
  ([u]
     (contacts-with-score-between u 61 300))
  ([u number]
     (take number (medium-contacts u))))

(defn weak-contacts
  ([u]
     (contacts-with-score-between u 0 60))
  ([u number]
     (take number (weak-contacts u))))


;; (defn contacts-of-strength [u strength-as-keyword]
;;   (condp = strength-as-keyword
;;     :strong (strong-contacts u)
;;     :medium (medium-contacts u)
;;     :weak (weak-contacts u)
;;     (throw+ {:type :severe :message (str "Unknown contact strength:" strength-as-keyword)})))

;; (defn- apply-selectors [selectors user]
;;   (if (empty? selectors)
;;     (:user/contacts user)
;;     (->> selectors
;;          distinct
;;          (map #(contacts-of-strength user %))
;;          (apply concat))))

;; (defn- apply-tags [tags contacts]
;;   contacts)

;; (defn- apply-offset [offset contacts]
;;   (if offset
;;     (drop offset contacts)
;;     contacts))

;; (defn- apply-limit [limit contacts]
;;   (if limit
;;     (take limit contacts)
;;     contacts))

;; (defn list-contacts [user options]
;;   (let [{:keys [selectors tags offset limit]} options]
;;     (->> (apply-selectors selectors user)
;;          (apply-tags tags)
;;          (apply-offset offset)
;;          (apply-limit limit))))