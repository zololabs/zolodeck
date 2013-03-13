(ns zolo.social.facebook.fb-api-test
    (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.utils.debug
        zolo.test.assertions.core
        conjure.core)
    (:require [zolodeck.clj-social-lab.facebook.core :as fb-lab]
              [zolo.social.facebook.gateway :as fb-gateway]
              [zolo.social.facebook.messages :as fb-messages]
              [zolo.social.facebook.stream :as fb-stream]              
              [zolodeck.utils.calendar :as zolo-cal]
              [zolo.setup.config :as conf]))

(def JACK-FB-ID "100005253966931")

(def START-TIMESTAMP (-> #inst "2000-10-22" .getTime zolo-cal/to-seconds))

(defn jack-access-token []
  (let [app-access-token (fb-gateway/app-access-token (conf/fb-app-id) (conf/fb-app-secret))
        all (fb-gateway/all-test-users (conf/fb-app-id) app-access-token)
        access-token (->> all (filter #(= JACK-FB-ID (:id %))) first :access-token)]
    (fb-gateway/extended-access-token access-token (conf/fb-app-id) (conf/fb-app-secret))))

(deftest ^:integration test-extended-user-info
  (fb-lab/in-facebook-lab
   (let [mary-dummy (fb-lab/create-user "Mary" "Poppins")
         jack-info (fb-gateway/extended-user-info (jack-access-token) JACK-FB-ID)
         mary-info (fb-lab/extended-user-info mary-dummy)]
     (is (same-value? (keys jack-info) (keys mary-info))))))

(deftest ^:integration test-friends-list
  (fb-lab/in-facebook-lab
   (let [mary (fb-lab/create-user "Mary" "Poppins")
         lamb (fb-lab/create-user "Little" "Lamb")
         goat (fb-lab/create-user "Goat" "Ey")]
     (fb-lab/make-friend mary lamb)
     (fb-lab/make-friend mary goat)
     (let [jack-friends-list (fb-gateway/friends-list (jack-access-token) JACK-FB-ID)
           mary-friends-list (fb-lab/fetch-friends mary)]
       (doseq [mf mary-friends-list]
         (is (same-value? (keys (first jack-friends-list)) (keys mf)))
         (is (same-value? (keys (get-in (first jack-friends-list) [:picture :data]))
                          (keys (get-in mf [:picture :data])))))))))

(deftest ^:integration test-fetch-inbox
  (fb-lab/in-facebook-lab
      (let [mickey (fb-lab/create-user "Mickey" "Mouse")
            donald (fb-lab/create-user "Donald" "Duck")
            daisy (fb-lab/create-user "Daisy" "Duck")]
        
        (fb-lab/login-as mickey)
        
        (fb-lab/make-friend mickey donald)
        (fb-lab/make-friend mickey daisy)

        (fb-lab/send-message mickey donald "1" "Hi, what's going on?" "2012-05-01")
        (fb-lab/send-message donald mickey "1" "Nothing, just work..." "2012-05-02")
        (fb-lab/send-message mickey donald "1" "OK, should I get groceries?" "2012-05-03")
        (fb-lab/send-message mickey daisy "2" "Hi, how's  it going?" "2012-06-01")
        (fb-lab/send-message daisy mickey "2" "Good, I finished writing the tests" "2012-06-02")

        (let [jack-messages (fb-messages/fetch-inbox (jack-access-token) START-TIMESTAMP)
              mickey-messages (fb-lab/fetch-messages mickey)]
          ;; TODO - test for subject when support is added to Zolodeck
          ;; TODO - test for attachment when support is added to Zolodeck
          (doseq [mm mickey-messages]
            (is (same-value? (remove #{:subject} (keys (first jack-messages)))
                             (remove #{:attachment} (keys mm)))))))))

(deftest ^:integration test-recent-activity
  (fb-lab/in-facebook-lab
   (let [mickey (fb-lab/create-user "Mickey" "Mouse")
         donald (fb-lab/create-user "Donald" "Duck")
         daisy (fb-lab/create-user "Daisy" "Duck")]
     
     (fb-lab/login-as mickey)
     
     (fb-lab/make-friend mickey donald)
     (fb-lab/make-friend mickey daisy)

     (fb-lab/post-to-wall mickey donald "hey check this link out" "2012-05-01")
     (fb-lab/post-to-wall mickey daisy "happy birthday" "2012-05-02")
     (fb-lab/post-to-wall daisy donald "check this picture" "2012-06-02")     

     (let [jack-feed (fb-stream/recent-activity (jack-access-token) JACK-FB-ID START-TIMESTAMP)
           mickey-feed (fb-lab/fetch-feeds donald)]
       (doseq [mf mickey-feed]
         (doseq [jfk (keys (first jack-feed))]
           (is (some #{jfk} (keys mf)))))
       ))))