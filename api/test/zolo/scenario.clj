(ns zolo.scenario
  (:use  [clojure.test :only [is are]]
         zolo.utils.debug
         zolo.test.web-utils
         zolo.test.core-utils
         zolo.scenarios.user
         conjure.core)
  (:require [zolo.domain.user :as user]
            [zolo.facebook.gateway :as gateway]
            [clojure.data.json :as json]))

(defn new-scenario []
  {:datomic true})

(defn decode-signed-request-for-scenarios [scenario]
  (fn [encoded-signed-request]
    (print-vals "Decoded SR" (when (:fb-user scenario) 
                               {:code "123" :user_id (get-in scenario [:fb-user :id])}))))

(defmacro with-scenario [scenario & body]
  `(with-redefs [gateway/decode-signed-request decode-signed-request-for-scenarios]
     (if INTEGRATION-TEST?
       (stubbing [gateway/code->token (get-in ~scenario [:fb-user :access-token])]
                 ~@body)
       (stubbing [user/load-from-fb (:fb-user ~scenario)]
                 ~@body))))

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

(defn run-web-request [scenario method url params-map]
  (let [response (web-request method url params-map)]
    (assoc scenario :web-response (assoc response :body (json/read-json (:body response))))))

(defn post-new-user 
  ([scenario]
     (post-new-user scenario (:current-user scenario)))
  ([scenario user]
     (with-scenario scenario
       (-> scenario
           (run-web-request :post (new-user-url) user)))))

(defn get-friends-list
  ([scenario]
     (get-friends-list scenario (:current-user scenario)))
  ([scenario user]
     (with-scenario scenario
       (-> scenario
           (run-web-request :get (friends-list-url) user)))))