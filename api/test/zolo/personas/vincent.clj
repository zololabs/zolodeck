(ns zolo.personas.vincent
  (:use zolodeck.utils.debug
        conjure.core)
  (:require [zolodeck.clj-social-lab.facebook.factory :as fb-factory]
            [zolo.facebook.gateway :as fb-gateway]
            [zolodeck.clj-social-lab.facebook.core :as fb]
            [zolo.facebook.inbox :as fb-inbox]
            [zolo.domain.user :as user]))

(defn create []
  (fb/in-facebook-lab
   (let [vincent (fb/create-user "Vincent" "Fong")
         jack (fb/create-user "Jack" "Daniels")
         jill (fb/create-user "Jill" "Ferry")]
     (user/insert-fb-user vincent)
    
     (fb/make-friend vincent jack)
     (fb/make-friend vincent jill)

     (stubbing [fb-gateway/friends-list (fb-factory/as-friends [jack jill])]
       (user/update-facebook-friends (:id vincent)))
    
     (fb/send-message vincent jack "1" "Hi, what's going on?" "2012-05-01")
     (fb/send-message jack vincent "1" "Nothing, just work..." "2012-05-02")
     (fb/send-message vincent jack "1" "OK, should I get groceries?" "2012-05-03")
    
     (fb/send-message vincent jill "2" "Hi, how's  it going?" "2012-06-01")
     (fb/send-message jill vincent "2" "Good, I finished writing the tests" "2012-06-02")
     (fb/send-message vincent jill "2" "OK, did you update the card?" "2012-06-03")
    
     (stubbing [fb-inbox/fetch-inbox (fb/fetch-messages vincent)]
       (user/update-facebook-inbox (:id vincent)))

     (user/find-by-fb-id (:id vincent)))))