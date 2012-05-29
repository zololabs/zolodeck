(ns zolo.factories.zolo-graph-factory)

(defn base []
  {"user-100"
   {:about 
    {:first-name "fname"
     :last-name "lname"
     :gender "male"
     :facebook {:link "fb-link"
                :username "fb-username"
                :email "email@email.com"
                :id "fb-id"
                :auth-token "fb-auth-token"}}
    :contacts [
               {"contact-101" 
                {:about 
                 {:first-name "contact1_fname"
                :last-name "contact1_lname"
                  :gender "male"
                :facebook {:id "fb-id1"
                           :link "fb-link1"
                           :birthday "10/1/1988"
                           :picture "picture-link1"}}

               :scores [{:value 20 :at 31231231231}
                        {:value 22 :at 31231231231}]
               
               :messages [{:id "message-100"
                           :platform "Facebook"
                           :mode "Message"
                           :text "Hey how are you?"
                           :date 12312312312
                           :from "user-100"
                           :to "contact-101"
                           :thread-id nil
                           :reply-to nil}
                          {:id "message-101"
                           :platform "Facebook"
                           :mode "Message"
                           :text "I am doing good"
                           :date 32342342342
                           :from "contact-101"
                           :to "user-100"
                           :thread-id "thread-1000"
                           :reply-to "message-100"}]}}

               {"contact-102" 
                {:about 
                 {:first-name "contact2_fname"
                  :last-name "contact2_lname"
                  :gender "male"
                  :facebook {:id "fb-id2"
                             :link "fb-link2"
                             :birthday "10/1/1988"
                             :picture "picture-link2"}}
                 
                 :scores [{:value 20 :at 31231231231}
                          {:value 22 :at 31231231231}]
                 
                 :messages [{:id "message-200"
                             :platform "Facebook"
                             :mode "Message"
                             :text "Hey how are you?"
                             :date 12312312312
                             :from "user-100"
                             :to "contact-101"
                             :thread-id nil
                             :reply-to nil}
                            {:id "message-201"
                             :platform "Facebook"
                             :mode "Message"
                             :text "I am doing good"
                             :date 32342342342
                             :from "contact-101"
                             :to "user-100"
                             :thread-id "thread-1000"
                             :reply-to "message-100"}]}}]}})