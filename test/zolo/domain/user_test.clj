;; (ns zolo.domain.user-test
;;   (:use zolodeck.demonic.test
;;         zolo.test.core-utils
;;         zolo.test.assertions
;;         zolodeck.utils.debug
;;         [clojure.test :only [run-tests deftest is are testing]])
;;   (:require [zolo.domain.user :as user]
;;             [zolo.personas.vincent :as vincent]
;;             [zolo.personas.loner :as loner]
;;             [zolo.personas.core :as personas]
;;             [zolo.personas.shy :as shy]))


;; (deftest test-update-scores
;;   (demonic-testing "when there are no scores before"
;;     (let [vincent (vincent/create)]
      
;;       (user/update-scores vincent)
      
;;       (let [vincent-reloaded (user/reload vincent)
;;             jack-reloaded (personas/friend-of vincent-reloaded "jack")
;;             jack-scores (:contact/scores jack-reloaded)]
        
;;         (is (= 1 (count jack-scores)))
;;         (is (= 30 (:score/value (first jack-scores))))
;;         (is (not (nil? (:score/at (first jack-scores))))))))
  
  ;; (demonic-testing "when there are scores already present"
  ;;   (let [vincent (vincent/create)]
  ;;     (user/update-scores vincent)

  ;;     (-> (user/reload vincent)
  ;;         user/update-scores)
      
  ;;     (let [vincent-reloaded (user/reload vincent)
  ;;           jack-reloaded (personas/friend-of vincent-reloaded "jack")
  ;;           jack-scores (:contact/scores jack-reloaded)]
        
  ;;       (is (= 2 (count jack-scores))))))

;;)