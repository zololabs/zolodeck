(ns zolo.api.server-api-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.demonic.test
        zolo.demonic.core
        zolo.utils.debug
        zolo.utils.clojure
        zolo.test.core-utils
        zolo.utils.http-status-codes)
  (:require [zolo.test.web-utils :as w-utils]
            [clojure.data.json :as json]
            [zolo.personas.shy :as shy]))

(demonictest test-server-status
  (let [resp (w-utils/web-request :get "/server/status")
        users-count (get-in resp [:body :no_of_users])]
    (is (= 200 (:status resp)))
    (is (zero? users-count)))

  (shy/create)
  
  (let [resp (w-utils/web-request :get "/server/status")
        users-count (get-in resp [:body :no_of_users])]
    (is (= 200 (:status resp)))
    (is (= 1 users-count))))