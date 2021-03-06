(ns zolo.personas.vincent
  (:use zolo.utils.debug
        zolo.demonic.test
        [clojure.test :only [run-tests deftest is are testing]]
        conjure.core)
  (:require [zolo.marconi.facebook.factory :as fb-factory]
            [zolo.facebook.gateway :as fb-gateway]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb]            
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.domain.user :as user]
            [zolo.personas.core :as personas]))

(defn create []
  (marconi/in-lab
   (let [vincent (personas/create-fb-user "Vincent" "Fong")
         jack (personas/create-fb-user "Jack" "Daniels")
         jill (personas/create-fb-user "Jill" "Ferry")]
     (user/insert-fb-user vincent)
    
     (fb/make-friend vincent jack)
     (fb/make-friend vincent jill)

     (personas/update-fb-friends vincent)

     (fb/send-message vincent jack "1" "Hi, what's going on?" "2012-05-01")
     (fb/send-message jack vincent "1" "Nothing, just work..." "2012-05-02")
     (fb/send-message vincent jack "1" "OK, should I get groceries?" "2012-05-03")
    
     (fb/send-message vincent jill "2" "Hi, how's  it going?" "2012-06-01")
     (fb/send-message jill vincent "2" "Good, I finished writing the tests" "2012-06-02")
    
     (personas/update-fb-inbox vincent)

     (user/find-by-fb-id (:id vincent)))))


(defn create-with-score []
  (user/update-scores (create)))