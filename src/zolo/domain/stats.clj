(ns zolo.domain.stats
  (:use zolodeck.utils.debug)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.utils.fe :as fe]            
            [zolo.domain.message :as message]
            [zolo.domain.score :as score]
            [zolodeck.utils.maps :as zolo-maps]
            [zolodeck.utils.math :as zolo-math]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn contacts-with-score-between [u lower upper]
  (filter #(and (>= (:contact/score %) lower)
                (< (:contact/score %) upper))
          (:user/contacts u)))

(defn contacts-stats [u]
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

(defn- recent-message-time [c]
  (->> c
       :contact/messages
       (sort-by :message/date)
       last
       :message/date))

(defn- no-messages? [c]
  (-> c :contact/messages empty?))

(defn forgotten-contacts [u ever-messaged? number]
  (let [contacts (->> u :user/contacts (sort-by recent-message-time))
        contacts (if ever-messaged?
                   (remove no-messages? contacts)
                   (filter no-messages? contacts))]
    (take number contacts)))

(defn all-messages-in-the-past [u num-days]
  (let [one-week-ago (time/minus (time/now) (time/days num-days))]
    (->> u
         :user/contacts
         (mapcat :contact/messages)
         (filter #(time/after? (time-coerce/to-date-time (:message/date %)) one-week-ago)))))

;; (defn all-messages-between-dates [u from-time to-time]
;;   (let [time-start (time/minus (time/now) (time/days num-days))]
;;     (->> u
;;          :user/contacts
;;          (mapcat :contact/messages)
;;          (filter #(time/after? (time-coerce/to-date-time (:message/date %)) one-week-ago))))  )

(defn all-message-count-in-the-past [u num-days]
  (count (all-messages-in-the-past u num-days)))

(defn message-count [u]
  (print-vals "MessageCount for" (:user/first-name u))
  (->> u
       :user/contacts
       (mapcat :contact/messages)
       count))

(defn network-stats [u]
  {:average (zolo-math/average (map :contact/score (:user/contacts u)))
   :messagecount (message-count u)
   ;;TODO This needs to be tested
   :strong-contacts (doall (map fe/format-contact (strong-contacts u 5)))   
   :weak-contacts (doall (map fe/format-contact (weak-contacts u 5)))
   :connect-soon (doall (map fe/format-contact (forgotten-contacts u true 5)))
   :never-contacted (doall (map fe/format-contact (forgotten-contacts u false 5)))
   :all-week-interaction-count (all-message-count-in-the-past u 7)
   :all-month-interaction-count (all-message-count-in-the-past u 31) 
   })