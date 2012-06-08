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

(defn upsert-contact [zg c]
  (update-in zg [(user-zolo-id zg) :contacts] #(merge % c)))

(defn messages [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id :messages]))

(defn all-messages [zg]
  (reduce (fn [acc c-id]
            (concat acc (messages zg c-id)))
          []
          (contact-zolo-ids zg)))

(defn add-message [zg c-id m]
  (update-in zg
             [(user-zolo-id zg) :contacts c-id :messages]
             #(conj % m)))

(defn scores [zg c-id]
  (get-in zg [(user-zolo-id zg) :contacts c-id :scores]))

(defn score [zg c-id]
  (last (sort-by :at (scores zg c-id))))

(defn has-score? [zg c-id]
  (not (nil? (score zg c-id))))

(defn score-value 
  ([s]
     (if (nil? s)
       -1
       (:value s)))
  ([zg c-id]
     (score-value (score zg c-id))))

(defn all-scores [zg]
  (reduce (fn [acc c-id]
            (concat acc (scores zg c-id)))
          []
          (contact-zolo-ids zg)))

(defn add-score [zg c-id s]
  (update-in zg
             [(user-zolo-id zg) :contacts c-id :scores]
             #(conj % s)))

(defn user->zolo-graph [user])



