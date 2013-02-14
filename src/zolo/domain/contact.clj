(ns zolo.domain.contact
  (:use zolo.setup.datomic-setup        
        zolo.utils.domain
        zolodeck.utils.debug
        [slingshot.slingshot :only [throw+ try+]])
  (:require [zolodeck.utils.string :as zolo-str]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolo.utils.logger :as logger]
            [zolo.utils.domain :as utils-domain]
            [zolo.social.core :as social]
            [zolo.domain.social-identity :as social-identity]
            [zolo.domain.accessors :as dom]
            [zolodeck.demonic.core :as demonic]
            [zolo.domain.score :as score]
            [clojure.set :as set]))

(defn contact-lookup-table [c]
  (->> (:contact/social-identities c)
      (map (fn [s] {(social-identity/social-identity-info s) c}))
      (apply merge)))

(defn contacts-lookup-table [contacts]
  (or (->> (map contact-lookup-table contacts)
           (apply merge))
      {}))

(defn find-contact-from-lookup [user social-identities]
  (let [contacts-lookup (contacts-lookup-table (:user/contacts user))]
    (->> social-identities
         (map social-identity/social-identity-info)
         (some #(contacts-lookup %)))))

(defn provider-info-by-provider [u]
  (->> u
       :user/contacts
       (mapcat :contact/social-identities)
       (map #(select-keys % [:social/provider :social/provider-uid]))
       (group-by :social/provider)))

(defn fresh-contacts-for-user-identity [user-identity]
  (let [{provider :identity/provider
         access-token :identity/auth-token
         provider-uid :identity/provider-uid} user-identity]
    (social/fetch-contacts provider access-token provider-uid "2012-10-22")))

;;TODO Duplication find-by-guid
(defn find-by-guid [guid]
  (when guid
    (-> (demonic/run-query '[:find ?c :in $ ?guid :where [?c :contact/guid ?guid]] guid)
        ffirst
        demonic/load-entity)))

(defn find-by-guid-string [guid-string]
  (when guid-string
    (find-by-guid (java.util.UUID/fromString guid-string))))

(defn reload [c]
  (find-by-guid (:contact/guid c)))

(defn fresh-contacts [u]
  (mapcat fresh-contacts-for-user-identity (:user/user-identities u)))

(defn update-contact [user fresh-contact]
  (let [contact (->> (:contact/social-identities fresh-contact)
                     (find-contact-from-lookup user))]
    (if contact
      (assoc contact :contact/social-identities
             (utils-domain/update-fresh-entities-with-db-id
               (:contact/social-identities contact)
               (:contact/social-identities fresh-contact)
               social-identity/social-identity-info
               :social/guid))
      fresh-contact)))

(defn update-contacts [user]
  (let [fresh-cs (fresh-contacts user)
        _ (logger/debug "Facebook returned" (count fresh-cs) "contacts for" (:user/first-name user))
        updated-contacts (map #(update-contact user  %) fresh-cs)]
    (-> (assoc user :user/contacts updated-contacts)
        demonic/insert)))

(defn update-score [ibc c]
  (-> (assoc c :contact/score (score/calculate ibc c))
      demonic/insert))

(defn set-muted [c muted?]
  (-> (assoc c :contact/muted muted?)
      demonic/insert))

(defn last-send-message [ibc c]
  (->> (ibc c)
       dom/messages-from-interactions
       (sort-by dom/message-date)
       last))

(defn is-contacted-on? [ibc c dt]
  (zolo-cal/same-day-instance? dt (dom/message-date-in-tz (last-send-message ibc c) (zolo-cal/time-zone-offset dt))))


(defn contact-score [c]
  (or (:contact/score c) 0))

(defn contacts-with-score-between [u lower upper]
  (->> (filter #(and (>= (contact-score %) lower) (<= (contact-score %) upper)) (:user/contacts u))
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

(defn contacts-of-strength [u strength-as-keyword]
  (condp = strength-as-keyword
    :strong (strong-contacts u)
    :medium (medium-contacts u)
    :weak (weak-contacts u)
    (throw+ {:type :severe :message (str "Unknown contact strength:" strength-as-keyword)})))

(defn- apply-selectors [selectors user]
  (if (empty? selectors)
    (:user/contacts user)
    (->> selectors
         distinct
         (map #(contacts-of-strength user %))
         (apply concat))))

(defn- apply-tags [tags contacts]
  contacts)


(defn- apply-offset [offset contacts]
  (if offset
    (drop offset contacts)
    contacts))

(defn- apply-limit [limit contacts]
  (if limit
    (take limit contacts)
    contacts))

(defn list-contacts [user options]
  (let [{:keys [selectors tags offset limit]} options]
    (->> (apply-selectors selectors user)
         (apply-tags tags)
         (apply-offset offset)
         (apply-limit limit))))

