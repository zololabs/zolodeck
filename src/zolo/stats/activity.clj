(ns zolo.domain.activity
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.accessors :as dom]
            [zolo.utils.fe :as fe]
            [zolo.stats.message-distribution :as md]
            [zolo.domain.message :as message]
            [zolo.domain.score :as score]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.calendar :as zolo-cal]
            [zolodeck.utils.math :as zolo-math]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn contact-score [c]
  (or (:contact/score c) 0))

(defn contacts-with-score-between [u lower upper]
  (filter #(and (>= (contact-score %) lower)
                (<  (contact-score %) upper))
          (:user/contacts u)))

(defn network-stats [u]
  {:total  (count (:user/contacts u))
   :strong (count (contacts-with-score-between u 250 10000000))
   :medium (count (contacts-with-score-between u 50 250))
   :weak   (count (contacts-with-score-between u 0 50))})

(defn strong-contacts [u number]
  (->> u
       :user/contacts
       (sort-by :contact/score)
       reverse
       (take number)))

(defn weak-contacts [u number]
  (->> u
       :user/contacts
       (sort-by :contact/score)
       (take number)))

(defn forgotten-contacts [imbc number]
  (let [contacts (-> imbc
                     (zolo-maps/select-keys-if #(empty? %2))
                     keys)]
    (take number contacts)))

(defn recent-message-time [[c messages]]
  (->> messages
       (sort-by :message/date)
       last
       :message/date))

(defn forgetting-contacts [imbc number]
  (let [imbc (-> imbc
                 (zolo-maps/select-keys-if #(not (empty? %2))))]
    (->> imbc
         (sort-by recent-message-time)
         keys
         (take number))))

(defn other-stats [u]
  (let [imbc (dom/inbox-messages-by-contacts u)]
    (merge {:averagescore (zolo-math/average (map contact-score (:user/contacts u)))
            :messagecount (count (:user/messages u))
            ;;TODO This needs to be tested
            :strong-contacts (domap #(fe/format-contact imbc %) (strong-contacts u 5))   
            :weak-contacts (domap #(fe/format-contact imbc %) (weak-contacts u 5))
            :connect-soon (domap #(fe/format-contact imbc %) (forgetting-contacts imbc 5))
            :never-contacted (domap #(fe/format-contact imbc %) (forgotten-contacts imbc 5))
            :all-week-interaction-count (dom/all-message-count-in-the-past imbc 7)
            :all-month-interaction-count (dom/all-message-count-in-the-past imbc 31)}
           (md/distribution-stats imbc))))

(defn- activity-text-to-display [a]
  (let [link (if-let [l (:message/link a)]
               (str "Shared a <a href='" l "'>link</a>"))]
    (or (a :message/text) (a :message/story) link)))

(defn- activity-picture-to-display [a]
  (or (a :message/picture) (a :message/icon)))

(defn- feed-item-for-display [c item]
  (let [contact-keys [:contact/guid :contact/first-name :contact/last-name :contact/score]
        message-keys [:message/text :message/date :message/story :message/icon :message/link :message/picture]]
    (-> (merge (select-keys c contact-keys)
               (select-keys item message-keys))
        ;(zolo-maps/update-val :message/date (fn [m k v] (zolo-cal/date-to-nice-string v)))
        (zolo-maps/update-val :message/text (fn [m k v] (activity-text-to-display m)))
        (zolo-maps/update-val :message/picture (fn [m k v] (activity-picture-to-display m)))
        ;(dissoc :message/story :message/link :message/icon)
        (zolo-maps/update-all-map-keys name))))

(defn- fmbc->list [fmbc]
  (->> fmbc
       (mapcat (fn [[c feed-items]] (map (partial feed-item-for-display c) feed-items)))))

(defn recent-activity [u]
  (->> u
       dom/feed-messages-by-contacts
       fmbc->list
       (reverse-sort-by #(% "date"))
       (take 50)))