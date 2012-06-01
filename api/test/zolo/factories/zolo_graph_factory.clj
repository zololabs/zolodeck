(ns zolo.factories.zolo-graph-factory
  (:use zolodeck.utils.clojure
        zolodeck.utils.debug)
  (:require [zolo.domain.zolo-graph :as zg]))

(defn user 
  ([zolo-id]
     {zolo-id
      {:zolo-id zolo-id
       :about 
       {:first-name (str "fname-" zolo-id)
        :last-name (str "lname-" zolo-id)
        :gender "male"
        :facebook {:link (str "fb-link-" zolo-id)
                   :username (str "fb-username-" zolo-id)
                   :email (str zolo-id "@email.com")
                   :id (str "fb-" zolo-id)
                   :auth-token (str "fb-auth-token-" zolo-id)}}
       :contacts {}}})
  ([]
     (user (str (random-guid)))))

(defn contact 
  ([zolo-id]
     {zolo-id
      {:zolo-id zolo-id
       :about 
       {:first-name (str zolo-id "_fname")
        :last-name (str zolo-id "_lname")
        :gender "male"
        :facebook {:id (str "fb-id" zolo-id)
                   :link (str "fb-link" zolo-id)
                   :birthday "10/1/1988"
                   :picture (str "picture-link" zolo-id)}}
       
       :scores []
       
       :messages []}})
  ([]
     (contact (str (random-guid)))))

(defn message 
  ([zolo-id]
     {:zolo-id zolo-id
      :platform "Facebook"
      :mode "Message"
      :text (str "This is message : " zolo-id)
      :date 12312312312
      :from "user-100"
      :to "contact-101"
      :thread-id nil
      :reply-to nil})
  ([]
     (message (str (random-guid)))))

(defn score 
  ([value at]
     {:value value :at at})
  ([value]
     (score value 31231231231))
  ([]
     (score (rand-int 100)))) 

(defn contact-zolo-id [c]
  (first (keys c)))

(defn add-message 
  ([c partial-msg]
     (update-in c
                [(contact-zolo-id c) :messages]
                #(merge % (merge (message) partial-msg))))
  ([c]
     (add-message c (message))))

(defn add-score 
  ([c partial-msg]
     (update-in c
                [(contact-zolo-id c) :scores]
                #(merge % (merge (score) partial-msg))))
  ([c]
     (add-score c (score))))

(defn add-contact 
  ([zg c]
     (zg/add-contact zg c))
  ([zg]
     (add-contact zg (contact))))

(defn contact-with-messages [c-id m-ids]
  (reduce (fn [c m-id]
            (add-message c (message m-id)))
          (contact c-id)
          m-ids))

(defn contact-with-scores [c-id score-values]
  (reduce (fn [c score-value]
            (add-score c (score score-value)))
          (contact c-id)
          score-values))

(defn add-contact-with-message-and-score [zg]
  (->> (contact)
       add-message
       add-score
       (add-contact zg)))
