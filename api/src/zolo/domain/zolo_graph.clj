(ns zolo.domain.zolo-graph
  (:use zolodeck.utils.debug))

(defn user-zolo-id [zg]
  (first (keys zg)))

;;TODO Need to define Zolo Contact Structure. Should it be with key or just value
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

(defn score-value [s]
  (:value s))

(defn all-scores [zg]
  (reduce (fn [acc c-id]
            (concat acc (scores zg c-id)))
          []
          (contact-zolo-ids zg)))

(defn add-score [zg c-id s]
  (update-in zg
             [(user-zolo-id zg) :contacts c-id :scores]
             #(conj % s)))

(defn load-from-datomic [])

(defn d3-node [name group]
  {"name" name
   "group" group})

(defn d3-link [target value]
  {"source" 0
   "target" target
   "value" value})

(defn d3-nodes [zg]
  (reduce (fn [acc [c-id c]]
            (conj acc (d3-node c-id 1)))
          [(d3-node (user-zolo-id zg) 1000)]
          (contacts zg)))

(defn d3-links [zg]
  (reduce (fn [acc c-id]
            (conj acc (d3-link 1 (-> (score zg c-id) score-value))))
          []
          (contact-zolo-ids zg)))

(defn format-for-d3 [zg]
  {"nodes" (d3-nodes zg)
   "links" (d3-links zg)})


