(ns zolo.domain.zolo-graph
  (:use zolodeck.utils.debug))

(defn user-zolo-id [zg]
  (first (keys zg)))

(defn contact-zolo-id [zg-contact]
  (first (keys zg-contact)))

(defn contact-zolo-ids [zg]
  (keys (get-in zg [(user-zolo-id zg) :contacts])))

(defn contacts [zg]
  (get-in zg [(user-zolo-id zg) :contacts]))

(defn contact [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id]))

(defn add-contact [zg c]
  (update-in zg [(user-zolo-id zg) :contacts] #(merge % c)))

;;TODO too much duplication to access messages and scores 
(defn messages [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id :messages]))

(defn all-messages [zg]
  (reduce (fn [acc c-id]
            (concat acc (messages zg c-id)))
          []
          (contact-zolo-ids zg)))

(defn scores [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id :scores]))

(defn all-scores [zg]
  (reduce (fn [acc c-id]
            (concat acc (scores zg c-id)))
          []
          (contact-zolo-ids zg)))


(defn load-from-datomic [])

(defn format-for-d3 [g]
  )


