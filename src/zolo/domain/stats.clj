(ns zolo.domain.stats
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

(defn contacts-with-score-between [u lower upper]
  (filter #(and (>= (:contact/score %) lower)
                (< (:contact/score %) upper))
          (:user/contacts u)))

(defn network-stats [u]
  {:total (count (:user/contacts u))
   :strong (count (contacts-with-score-between u 250 10000000))
   :medium (count (contacts-with-score-between u 50 250))
   :weak (count (contacts-with-score-between u 0 50))})

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
    (merge {:averagescore (zolo-math/average (map :contact/score (:user/contacts u)))
            :messagecount (count (:user/messages u))
            ;;TODO This needs to be tested
            :strong-contacts (domap #(fe/format-contact imbc %) (strong-contacts u 5))   
            :weak-contacts (domap #(fe/format-contact imbc %) (weak-contacts u 5))
            :connect-soon (domap #(fe/format-contact imbc %) (forgetting-contacts imbc 5))
            :never-contacted (domap #(fe/format-contact imbc %) (forgotten-contacts imbc 5))
            :all-week-interaction-count (dom/all-message-count-in-the-past imbc 7)
            :all-month-interaction-count (dom/all-message-count-in-the-past imbc 31)}
           (md/distribution-stats imbc))))

(defn recent-activity [u]
  (-> u
      dom/feed-messages-by-contacts
      (zolo-maps/transform-vals-with (fn [_ v] (dom/messages-in-the-past 7 v)))
      (zolo-maps/update-all-map-keys #(select-keys % [:contact/first-name :contact/last-name :contact/score]))))