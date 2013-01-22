(ns zolo.scenario
  (:use  [clojure.test :only [is are]]
         zolodeck.utils.debug
         conjure.core
         zolo.test.web-utils
         zolo.test.core-utils
         zolo.scenarios.user)
  (:require [zolo.domain.user :as user]
            [zolo.web.fb-auth :as fb-auth]
            [clojure.data.json :as json]
            [zolo.test.assertions :as assertions]))

(defn new-scenario []
  {:datomic true})

(defn return-first-arg [& args] (first args))

(defn return-last-arg [& args] (last args))

(defn decode-signed-request-for-scenarios [scenario]
  (fn [encoded-signed-request]
    (when (:fb-user scenario) 
      {:code "123" :user_id (get-in scenario [:authResponse :userID])})))

(defmacro with-scenario [scenario & body]
  `(stubbing [fb-auth/decode-signed-request decode-signed-request-for-scenarios
              user/update-with-extended-fb-auth-token return-first-arg]
     ~@body))

(defn login-as-valid-facebook-user 
  ([scenario]
     (login-as-valid-facebook-user scenario default-fb-login-credentials))
  ([scenario user]
     (assoc scenario :fb-user user)
     (assoc scenario :current-user user)))

(defn assert-user-in-datomic [scenario assertion-function]
  (with-scenario scenario 
    (-> scenario
        (get-in [:current-user :providerLoginInfo :authResponse :userID])
        user/find-by-login-provider-uid
        assertion-function))
  scenario)

(defn assert-user-present-in-datomic [scenario]
  (assert-user-in-datomic scenario assertions/assert-datomic-id-present))

(defn jsonify-body-if-needed [response]
  (if (not (empty? (:body response)))
    (assoc response :body (json/read-json (:body response)))
    response))

(defn run-web-request [scenario method url params-map]
  (->> (web-request method url params-map)
       jsonify-body-if-needed
       (assoc scenario :web-response)))

(defn post-new-user 
  ([scenario]
     (post-new-user scenario (:current-user scenario)))
  ([scenario user]
     (with-scenario scenario
       (-> scenario
           (run-web-request :post (new-user-url) user)))))
