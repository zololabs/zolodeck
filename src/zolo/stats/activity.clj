(ns zolo.stats.activity
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
            [clj-time.coerce :as ctc]))

(defn contact-score [c]
  (or (:contact/score c) 0))

(defn contacts-with-score-between [u lower upper]
  (filter #(and (>= (contact-score %) lower)
                (<  (contact-score %) upper))
          (:user/contacts u)))

(defn not-contacted-for-days [imbc days]
  (let [now (zolo-cal/now-joda)
        selector-fn (fn [c msgs]
                      (if-let [d (-> msgs last :message/date)]
                        (-> d
                            ctc/to-date-time
                            (time/plus (time/days days))
                            (.isBefore now))))
        last-contacted-in (zolo-maps/select-keys-if imbc selector-fn)]
    (keys last-contacted-in)))

(defn network-stats [u imbc]
  {:total  (count (:user/contacts u))
   :strong (count (contacts-with-score-between u 250 10000000))
   :medium (count (contacts-with-score-between u 50 250))
   :weak   (count (contacts-with-score-between u 0 50))
   :quartered (count (not-contacted-for-days imbc 90))})

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

(defn- message-filter-fn-for-days-within [num-days]
  (if (neg? num-days)
    (constantly true)
    (let [time-diff (time/minus (time/now) (time/days num-days))]
      #(time/after? (ctc/to-date-time (dom/message-date %)) time-diff))))

(defn all-interactions-in-the-past [ibc num-days]
  (->> ibc
       vals
       (apply concat)
       (keep last)
       (filter (message-filter-fn-for-days-within num-days))))

(defn all-interaction-count-in-the-past [ibc num-days]
  (->> (all-interactions-in-the-past ibc num-days)
       count))

(defn daily-counts [msgs]
  (let [msgs-dates (map #(zolo-cal/start-of-day-inst (dom/message-date %)) msgs)
        msgs-freq (frequencies msgs-dates)
        all-dates (-> msgs-dates first zolo-cal/all-dates-through-today)]
    (reduce (fn [ret date]
              (conj ret [(zolo-cal/date-to-simple-string date) (or (msgs-freq date) 0)])) [] all-dates)))

(defn connect-soon-contacts [imbc]
  (let [contacts (forgetting-contacts imbc 5)
        prepare-contact (fn [c]
                          (let [msgs (dom/messages-for-contact imbc c)]
                            (merge (fe/format-contact imbc c)
                                   {:interactions (daily-counts msgs)})))]
    (domap prepare-contact contacts)))

(defn other-stats [u imbc ibc]
  (merge {:averagescore (zolo-math/average (map contact-score (:user/contacts u)))
          :messagecount (count (:user/messages u))
          ;;TODO This needs to be tested
          :strong-contacts (domap #(fe/format-contact imbc %) (strong-contacts u 5))   
          :weak-contacts (domap #(fe/format-contact imbc %) (weak-contacts u 5))
          :connect-soon (connect-soon-contacts imbc)
          :never-contacted (domap #(fe/format-contact imbc %) (forgotten-contacts imbc 5))
          :all-week-interaction-count (all-interaction-count-in-the-past ibc 7)
          :all-month-interaction-count (all-interaction-count-in-the-past ibc 31)}
         (md/distribution-stats imbc)))

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

(defn daily-counts-for-network [imbc]
  (-> imbc
      dom/messages-from-imbc
      daily-counts))