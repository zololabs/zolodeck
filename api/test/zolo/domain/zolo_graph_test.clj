(ns zolo.domain.zolo-graph-test
  (:use [zolo.domain.zolo-graph :as zolo-graph]
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.factories.zolo-graph-factory :as zg-factory]))


(deftest test-zolo-graph-validation
  (is (zolo-graph/valid? (zg-factory/base))))

