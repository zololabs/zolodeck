(ns zolo.factories.zolo-graph-factory
  (:use zolodeck.utils.clojure
        zolodeck.utils.debug)
  (:require [zolo.domain.zolo-graph :as zg]
            [zolo.domain.zolo-graph.validation :as zg-validation]))

(def ^:dynamic ZG-ATOM)

(defn default-message 
  ([guid]
     {:guid guid
      :message-id (str "msg-" guid)
      :platform "Facebook"
      :mode "Message"
      :text (str "This is message : " guid)
      :date #inst "1980-08-08T00:00:00.000-00:00"
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
  ([guid]
     {guid
      {:guid guid
       :about 
       {:first-name (str "fname-" guid)
        :last-name (str "lname-" guid)
        :gender "male"
        :facebook {:link (str "fb-link-" guid)
                   :username (str "fb-username-" guid)
                   :email (str guid "@email.com")
                   :id (str "fb-" guid)
                   :auth-token (str "fb-auth-token-" guid)}}
       :contacts {}}})
  ([]
     (new-user (random-guid))))

(defn new-contact 
  ([guid]     
     {guid
      {:guid guid
       :about 
       {:first-name (str guid "_fname")
        :last-name (str guid "_lname")
        :gender "male"
        :facebook {:id (str "fb-id" guid)
                   :link (str "fb-link" guid)
                   :birthday #inst "1980-08-08T00:00:00.000-00:00"
                   :picture (str "picture-link" guid)}}
       
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
          (let [u-fb-id (zg/user-fb-id @ZG-ATOM)
                c-id (zg/contact-guid to)
                c-fb-id (zg/contact-fb-id to)
                m (merge (default-message) {:from u-fb-id :to c-fb-id :text msg})]
            (zg/add-message @ZG-ATOM c-id m))))

(defn receive-message [from msg]
  (reset! ZG-ATOM 
          (let [u-fb-id (zg/user-fb-id @ZG-ATOM)
                c-id (zg/contact-guid from)
                c-fb-id (zg/contact-fb-id from)
                m (merge (default-message) {:from c-fb-id :to u-fb-id :text msg})]
            (zg/add-message @ZG-ATOM c-id m))))

(defn add-score 
  ([c score-value score-at]
     (reset! ZG-ATOM
             (zg/add-score @ZG-ATOM (zg/contact-guid c) (default-score score-value score-at))))
  ([c score-value]
     (add-score c score-value 31231231231))
  ([c]
     (add-score c (rand-int 100))))

(defmacro building [u & build-forms]
  `(binding [ZG-ATOM (atom ~u)]
     ~@build-forms
     (zg-validation/assert-zolo-graph @ZG-ATOM)))