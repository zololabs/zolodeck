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

(defn not-contacted-for-days [imbc days]
  (let [now (zolo-cal/now-joda)
        selector-fn (fn [c msgs]
                      (if (empty? msgs)
                        true
                        (if-let [d (->> msgs (sort-by dom/message-date) last dom/message-date)]
                          (-> d
                              ctc/to-date-time
                              (time/plus (time/days days))
                              (.isBefore now)))))
        last-contacted-in (zolo-maps/select-keys-if imbc selector-fn)]
    (keys last-contacted-in)))

(defn network-stats [u imbc]
  {:total  (count (:user/contacts u))
   :strong (count (contact/strong-contacts u))
   :medium (count (contact/medium-contacts u))
   :weak   (count (contact/weak-contacts u))
   :quartered (count (not-contacted-for-days imbc 90))})

(defn forgotten-contacts [ibc number]
  (let [contacts (-> ibc
                     (zolo-maps/select-keys-if #(empty? %2))
                     keys)]
    (take number contacts)))

(defn recent-message-time [[c interactions]]
  (->> interactions
       dom/messages-from-interactions
       (sort-by dom/message-date)
       last
       dom/message-date))

(defn forgetting-contacts [ibc number]
  (let [ibc (zolo-maps/select-keys-if ibc #(not (empty? %2)))]
    (->> ibc
         (sort-by recent-message-time)
         keys
         (remove :contact/muted)
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

;;TODO Move all these suggestions to its own namespace
(defn suggestion-set-name [client-date]
  (str "ss-"
       (zolo-cal/year-from-instant client-date) "-"
       (zolo-cal/month-from-instant client-date) "-"
       (zolo-cal/date-from-instant client-date)))

(defn format-suggested-contact [client-date ibc c]
  (let [interactions (ibc c)]
    (merge (fe/format-contact ibc c)
           {:contacted-today (contact/is-contacted-on? ibc c client-date)
            :interactions (daily-counts interactions)})))

(defn connect-soon-contacts [u ibc client-date]
  (let [set-name (suggestion-set-name client-date)
        suggested-contacts (user/suggestion-set u set-name)
        contacts (if (empty? suggested-contacts)
                   (user/new-suggestion-set u set-name (forgetting-contacts ibc 5))
                   suggested-contacts)]
    (domap #(format-suggested-contact client-date ibc %) contacts)))

(defn other-stats [u ibc client-date]
  (merge {:averagescore (zolo-math/average (map contact/contact-score (:user/contacts u)))
          :messagecount (count (:user/messages u))
          :strong-contacts (domap #(fe/format-contact ibc %) (contact/strong-contacts u 5))   
          :weak-contacts (domap #(fe/format-contact ibc %) (contact/weak-contacts u 5))
          :connect-soon (connect-soon-contacts u ibc client-date)
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