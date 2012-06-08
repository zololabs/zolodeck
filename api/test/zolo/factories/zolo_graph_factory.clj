(ns zolo.factories.zolo-graph-factory
  (:use zolodeck.utils.clojure
        zolodeck.utils.debug)
  (:require [zolo.domain.zolo-graph :as zg]
            [zolo.domain.zolo-graph.validation :as zg-validation]))

(def ^:dynamic ZG-ATOM)

(defn guid [four-letters-four-digits]
  (java.util.UUID/fromString (str four-letters-four-digits "-1000-413f-8a7a-f11c6a9c4036")))

(defn default-message 
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
     (default-message (random-guid))))

(defn default-score 
  ([value at]
     {:value value :at at})
  ([value]
     (default-score value 31231231231))
  ([]
     (default-score (rand-int 100))))

(defn new-user 
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
     (new-user (random-guid))))

(defn new-contact 
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
     (new-contact (random-guid))))


(defn add-contact 
  ([c]
     (reset! ZG-ATOM (zg/upsert-contact @ZG-ATOM c))
     nil)
  ([]
     (add-contact (new-contact))))

(defn send-message [to msg]
  (reset! ZG-ATOM 
          (let [u-id (zg/user-zolo-id @ZG-ATOM)
                c-id (zg/contact-zolo-id to)
                m (merge (default-message) {:from u-id :to c-id :text msg})]
            (zg/add-message @ZG-ATOM c-id m))))

(defn receive-message [from msg]
  (reset! ZG-ATOM 
          (let [u-id (zg/user-zolo-id @ZG-ATOM)
                c-id (zg/contact-zolo-id from)
                m (merge (default-message) {:from c-id :to u-id :text msg})]
            (zg/add-message @ZG-ATOM c-id m))))

(defn add-score 
  ([c score-value score-at]
     (reset! ZG-ATOM
             (zg/add-score @ZG-ATOM (zg/contact-zolo-id c) (default-score score-value score-at))))
  ([c score-value]
     (add-score c score-value 31231231231))
  ([c]
     (add-score c (rand-int 100))))

(defmacro building [u & build-forms]
  `(binding [ZG-ATOM (atom ~u)]
     ~@build-forms
     (zg-validation/assert-zolo-graph @ZG-ATOM)))