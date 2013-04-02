(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolo.utils.debug
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolo.utils.string :as zolo-str]
            [zolo.utils.maps :as zolo-maps]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.utils.logger :as logger]
            [zolo.utils.domain :as utils-domain]
            [zolo.social.core :as social]
            [zolo.demonic.core :as demonic]
            [zolo.domain.social-identity :as si]
            [zolo.domain.score :as score]
            [clojure.set :as set]))

;; (defn- contact-lookup-table [c]
;;   (->> (:contact/social-identities c)
;;       (map (fn [s] {(si/social-identity-info s) c}))
;;       (apply merge)))

;; (defn- contacts-lookup-table [contacts]
;;   (or (->> (map contact-lookup-table contacts)
;;            (apply merge))
;;       {}))

;; (defn- find-contact-from-lookup [user social-identities]
;;   (let [contacts-lookup (contacts-lookup-table (:user/contacts user))]
;;     (->> social-identities
;;          (map si/social-identity-info)
;;          (some #(contacts-lookup %)))))

(defn- base-contact [si]
  {:contact/social-identities [si]})

(defn- contact-has-si? [c si]
  (some #(= (si/social-identity-info %)
            (si/social-identity-info si))
        (:contact/social-identities c)))

(defn- find-contact-with-si [cs si]
  (-> (filter #(contact-has-si? % si) cs)
      first))

(defn- update-si-in-contact [c fresh-si]
  (let [updated-si (-> (:contact/social-identities c)
                       (si/social-identity (si/social-identity-info fresh-si))
                       (merge fresh-si))]
    (zolo-maps/update-in-when c
                              [:contact/social-identities]
                              #(si/has-id? % (si/social-identity-info fresh-si))
                              updated-si)))

(defn- update-contacts-with-si [cs fresh-si]
  (if-let [c (find-contact-with-si cs fresh-si)]
    (map (fn [c] (if (contact-has-si? c fresh-si)
                  (update-si-in-contact c fresh-si)
                  c))
         cs)
    (->> fresh-si
         base-contact
         (conj cs))))

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

(defn updated-contacts [cs sis]
  (if (empty? sis)
    cs
    (-> cs
        (update-contacts-with-si (first sis))
        (updated-contacts (rest sis)))))


;; (defn provider-info-by-provider [u]
;;   (->> u
;;        :user/contacts
;;        (mapcat :contact/social-identities)
;;        (map #(select-keys % [:social/provider :social/provider-uid]))
;;        (group-by :social/provider)))


;; ;;TODO Duplication find-by-guid
;; (defn find-by-guid [guid]
;;   (when guid
;;     (-> (demonic/run-query '[:find ?c :in $ ?guid :where [?c :contact/guid ?guid]] guid)
;;         ffirst
;;         demonic/load-entity)))

;; (defn find-by-guid-string [guid-string]
;;   (when guid-string
;;     (find-by-guid (java.util.UUID/fromString guid-string))))

;; (defn reload [c]
;;   (find-by-guid (:contact/guid c)))

;;TODO test
(defn update-score [ibc c]
  (-> (assoc c :contact/score (score/calculate ibc c))
      demonic/insert))

;; (defn set-muted [c muted?]
;;   (-> (assoc c :contact/muted muted?)
;;       demonic/insert))

;; (defn last-send-message [ibc c]
;;   (->> (ibc c)
;;        dom/messages-from-interactions
;;        (sort-by dom/message-date)
;;        last))

;; (defn is-contacted-on? [ibc c dt]
;;   (zolo-cal/same-day-instance? dt (dom/message-date-in-tz (last-send-message ibc c) (zolo-cal/time-zone-offset dt))))

;; (defn contact-score [c]
;;   (or (:contact/score c) 0))

;; (defn contacts-with-score-between [u lower upper]
;;   (->> (filter #(and (>= (contact-score %) lower) (<= (contact-score %) upper)) (:user/contacts u))
;;        (sort-by :contact/score)
;;        reverse))

;; (defn strong-contacts
;;   ([u]
;;      (contacts-with-score-between u 301 1000000))
;;   ([u number]
;;      (take number (strong-contacts u))))

;; (defn medium-contacts
;;   ([u]
;;      (contacts-with-score-between u 61 300))
;;   ([u number]
;;      (take number (medium-contacts u))))

;; (defn weak-contacts
;;   ([u]
;;      (contacts-with-score-between u 0 60))
;;   ([u number]
;;      (take number (weak-contacts u))))

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

;; (defn format [c ibc client-date]
;;   (print-vals "IBC " ibc)
;;   (let [interactions (ibc c)
;;         si (first (:contact/social-identities c))]
;;     {:first-name (:contact/first-name c)
;;      :last-name (:contact/last-name c)
;;      :guid (str (:contact/guid c))
;;      :muted (:contact/muted c)
;;      :picture-url (:social/photo-url si)
;;      :contacted-today (is-contacted-on? ibc c client-date)}))


;;TODO test this
(defn distill [c]
  (when c
    {:contact/first-name (first-name c)
     :contact/last-name (last-name c)
     :contact/guid (:contact/guid c)
     ;;:muted (:contact/muted c)
     :contact/picture-url (picture-url c)
     ;;     :contacted-today (is-contacted-on? ibc c client-date)
     ;; :interactions (daily-counts interactions);
     }))
