(ns zolo.stats.activity
  (:use zolodeck.utils.debug
        zolodeck.utils.clojure)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.domain.accessors :as dom]
            [zolo.utils.fe :as fe]
            [zolo.stats.interaction-distribution :as int-d]
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
  (->> (filter #(and (>= (contact-score %) lower) (<  (contact-score %) upper)) (:user/contacts u))
       (sort-by :contact/score)
       reverse))

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

(defn network-stats [u imbc]
  {:total  (count (:user/contacts u))
   :strong (count (strong-contacts u))
   :medium (count (medium-contacts u))
   :weak   (count (weak-contacts u))
   :quartered (count (not-contacted-for-days imbc 90))})

(defn forgotten-contacts [ibc number]
  (let [contacts (-> ibc
                     (zolo-maps/select-keys-if #(empty? %2))
                     keys)]
    (take number contacts)))

(defn recent-message-time [[c interactions]]
  (->> interactions
       dom/messages-from-interactions
       (sort-by :message/date)
       last
       :message/date))

(defn forgetting-contacts [ibc number]
  (let [ibc (zolo-maps/select-keys-if ibc #(not (empty? %2)))]
    (->> ibc
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
       dom/interactions-from-ibc
       (keep last)
       (filter (message-filter-fn-for-days-within num-days))))

(defn all-interaction-count-in-the-past [ibc num-days]
  (->> (all-interactions-in-the-past ibc num-days)
       count))

(defn daily-counts [interactions]
  (let [interactions-dates (map #(zolo-cal/start-of-day-inst (dom/interaction-date %)) interactions)
        interactions-freq (frequencies interactions-dates)
        all-dates (-> interactions-dates first zolo-cal/all-dates-through-today)]
    (reduce (fn [ret date]
              (conj ret [(zolo-cal/date-to-simple-string date) (or (interactions-freq date) 0)])) [] all-dates)))

(defn connect-soon-contacts [ibc]
  (let [contacts (forgetting-contacts ibc 5)
        prepare-contact (fn [c]
                          (let [interactions (ibc c)]
                            (merge (fe/format-contact ibc c)
                                   {:interactions (daily-counts interactions)})))]
    (domap prepare-contact contacts)))

(defn other-stats [u ibc]
  (merge {:averagescore (zolo-math/average (map contact-score (:user/contacts u)))
          :messagecount (count (:user/messages u))
          ;;TODO This needs to be tested
          :strong-contacts (domap #(fe/format-contact ibc %) (strong-contacts u 5))   
          :weak-contacts (domap #(fe/format-contact ibc %) (weak-contacts u 5))
          :connect-soon (connect-soon-contacts ibc)
          :never-contacted (domap #(fe/format-contact ibc %) (forgotten-contacts ibc 5))
          :all-week-interaction-count (all-interaction-count-in-the-past ibc 7)
          :all-month-interaction-count (all-interaction-count-in-the-past ibc 31)}
         (int-d/distribution-stats ibc)))

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

(defn daily-counts-for-network [ibc]
  (-> ibc
      dom/interactions-from-ibc
      daily-counts))