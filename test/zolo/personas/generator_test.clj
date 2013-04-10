(ns zolo.personas.generator-test
  (:use zolo.utils.debug
        zolo.utils.clojure
        [clojure.test :only [run-tests deftest is are testing]])
  (:require [zolo.personas.generator :as pgen]))

;; (deftest test-number-of-user-identities
;;   {:SPECS {} :UI-IDS-ALLOWED [:FACEBOOK :EMAIL] :UI-COUNT 5})