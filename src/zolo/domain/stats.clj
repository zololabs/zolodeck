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

(defn recent-message-time [u c]
  (->> c
       (dom/contact-messages u)
       (sort-by :message/date)
       last
       :message/date))

(defn oldest-message-time [u c]
  (->> c
       (dom/contact-messages u)
       (reverse-sort-by :message/date)
       last
       :message/date))

(defn no-messages? [u c]
  (->> c
       (dom/contact-messages u)
       empty?))

(defn forgotten-contacts [u ever-messaged? number]
  (let [contacts (->> u :user/contacts (sort-by #(recent-message-time u %)))
        contacts (if ever-messaged?
                   (remove #(no-messages? u %) contacts)
                   (filter #(no-messages? u %) contacts))]
    (take number contacts)))

(defn all-messages-in-the-past [u num-days]
  (let [one-week-ago (time/minus (time/now) (time/days num-days))]
    (->> u
         :user/messages
         (filter #(time/after? (time-coerce/to-date-time (:message/date %)) one-week-ago)))))

(defn all-message-count-in-the-past [u num-days]
  (count (all-messages-in-the-past u num-days)))

(defn other-stats [u]
  (merge {:average (zolo-math/average (map :contact/score (:user/contacts u)))
          :messagecount (count (:user/messages u))
          ;;TODO This needs to be tested
          :strong-contacts (domap #(fe/format-contact u %) (strong-contacts u 5))   
          :weak-contacts (domap #(fe/format-contact u %) (weak-contacts u 5))
          :connect-soon (domap #(fe/format-contact u %) (forgotten-contacts u true 5))
          :never-contacted (domap #(fe/format-contact u %) (forgotten-contacts u false 5))
          :all-week-interaction-count (all-message-count-in-the-past u 7)
          :all-month-interaction-count (all-message-count-in-the-past u 31)}
         (md/distribution-stats u)))