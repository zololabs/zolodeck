(ns zolo.social.facebook.fb-api-test
    (:use [clojure.test :only [run-tests deftest is are testing]]
        zolodeck.utils.debug
        zolo.test.assertions.core
        conjure.core)
    (:require [zolodeck.clj-social-lab.facebook.core :as fb-lab]
              [zolo.social.facebook.gateway :as fb-gateway]
              [zolo.setup.config :as conf]))

(def JACK-FB-ID "100005253966931")

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

;; test-fetch-inbox

;; test-recent-activity