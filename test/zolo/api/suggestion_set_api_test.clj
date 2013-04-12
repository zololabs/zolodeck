(ns zolo.api.suggestion-set-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        conjure.core
        zolo.test.core-utils
        zolo.utils.clojure
        zolo.demonic.test
        zolo.demonic.core)
  (:require [zolo.personas.factory :as personas]
            [zolo.personas.shy :as shy-persona]
            [zolo.utils.calendar :as zolo-cal]
            [zolo.test.web-utils :as w-utils]))

(deftest test-find-suggestion-sets
  (demonic-testing "when user is not present, it should return 404"
    (let [resp (w-utils/web-request :get (str "/users/" (random-guid-str) "/suggestion_sets") {})]
      (is (= 404 (:status resp)))))

  (demonic-testing "when user is present, it should return distilled ss"
    (run-as-of "2012-12-21"
      
      (let [shy (shy-persona/create)
            resp (w-utils/web-request :get (str "/users/" (:user/guid shy) "/suggestion_sets") {})]

        (is (= 200 (:status resp)))

        (is (= "ss-2012-12-21" (get-in resp [:body :name])))

        (is (= 2 (count (get-in resp [:body :contacts]))))))))

;; (deftest test-create-activity-with-categories
;;   (refresh-db)
;;   (let [amit (setup-base-data)]
;;     (with-auth-header "amit@currylogic.com" "123456"
;;       (is (= 2 (c/record-count)))
;;       (post-request (new-activity-url amit) 
;;                     {:title "hopscotch" :timing (now-for-test) :address "backyard" :categories "play,home , free"})
;;       (is (= 5 (c/record-count)))
;;       (let [hopscotch (a/find-by-title "hopscotch")]
;;         (is-same-seq? '("play" "home" "free") (ac/category-labels hopscotch))))))

;; (defn suggestion-set-url [user ss-name]
;;   (str "/users/" (:user/guid user) "/suggestion_sets/" ss-name))

;; (deftest test-get-suggestion-set
;;   (demonic-testing "When suggestion set is found"
;;     (let [shy (shy/create)
;;           ]

;;       (print-vals shy)
;;       (ss/new-suggestion-set shy "ss-2013-3-13")
;;       (print-vals (user/reload shy))
      
;;       (let [response (get-request (suggestion-set-url shy "ss-2013-3-13"))]
;;         (print-vals response))

;;       ))
  
;;   (demonic-testing "When suggestion set is NOT found"))