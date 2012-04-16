(ns zolo.scenario
  (:use  [clojure.test :only [is are]]
         zolo.utils.debug
         zolo.test.web-utils
         zolo.test.core-utils
         zolo.scenarios.user
         org.rathore.amit.conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.facebook.gateway :as gateway]))

(defn new-scenario []
  {:datomic true})

(defn mocked-decode-signed-request [scenario encoded-signed-request]
  (when (:fb-user scenario) 
    {:code "123" :user_id (get-in scenario [:fb-user :id])}))

(defmacro with-scenario [scenario & body]
  `(binding [gateway/decode-signed-request (partial mocked-decode-signed-request ~scenario)]
     (stubbing [user/load-from-fb (:fb-user ~scenario)]
       ~@body)))

(defn assert-user-in-datomic [scenario assertion-function]
  (with-scenario scenario 
    (-> scenario
        :fb-user
        :id
        user/find-by-fb-id
        assertion-function))
  scenario)

(defn assert-user-not-present-in-datomic [scenario]
  (assert-user-in-datomic scenario assert-datomic-id-not-present))

(defn assert-user-present-in-datomic [scenario]
  (assert-user-in-datomic scenario assert-datomic-id-present))

(defn login-as-valid-facebook-user 
  ([scenario]
     (login-as-valid-facebook-user scenario default-fb-user))
  ([scenario user]
     (assoc scenario :fb-user user)))

(defn post-new-user 
  ([scenario]
     (post-new-user scenario (:current-user scenario)))
  ([scenario user]
     (with-scenario scenario
       (-> scenario
           (assoc :web-response (web-request :post (new-user-url) user))))))