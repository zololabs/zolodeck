(ns zolo.test.compojure-utils
  (:use clojure.test
        zolodeck.utils.string
        zolodeck.utils.debug
        zolodeck.utils.maps)
  (:require [zolo.core1 :as services]))

(def *testing-trace* true)

(defn compojure-request [method resource stringified-params]
  {:request-method method 
   :uri resource 
   :params stringified-params
   :headers {"accept" "application/vnd.zololabs.zolodeck.v1+json"}})

(defn check-response-status [verb response resource expected-code stringified-params]
  (let [actual-code (:status response)
        success (= expected-code actual-code)
        verb-name (.toUpperCase (name verb))]
    (if (and (= actual-code 200) (not= expected-code 200))
      (println (.toUpperCase (name verb)) "of resource" resource "did not fail!"))
    (if (and (= actual-code 400) (= expected-code 200))
      (println verb-name "to resource" resource "UNSUCCESSFUL!\nINPUT:" stringified-params "\nOUTPUT:" response))
    (if (and (= actual-code 500) (= expected-code 200))
      (println "Resource ERROR trying to" verb-name resource "\nOUTPUT:" response))
    (is success)))

(defn request-successful? [verb response resource stringified-params]
  (check-response-status verb response resource 200 stringified-params))

(defn check-unsuccesful-response-status [verb response resource expected-code stringified-params]
  (check-response-status verb response resource expected-code stringified-params)
  (if-not (:body response)
    (println "Cause of resource" resource "failure is missing in body:" response))
  (is (not (empty? (:body response)))))

(defn request-unsuccessful? [verb response resource stringified-params]
  (check-unsuccesful-response-status verb response resource 400 stringified-params))

(defn request-error? [verb response resource stringified-params]
  (check-unsuccesful-response-status verb response resource 500 stringified-params))

;; (defn print-auth-failure-message []
;;   (if *testing-trace*
;;     (let [[auth-method encoded] (.split *auth-header* " ")
;;           [email password] (.split (b64/decode-str encoded) ":")]
;;       (println "AUTHORIZATION FAILURE! Auth header was:" *auth-header* "[" auth-method "]" (str email ":" password)))))

(defn web-request [& [method resource params expected-response-code]]
  (if *testing-trace*
    (println "\n***** Testing webmethod:" (up-key-name method) 
             "on" resource "with params:" params "expecting response status:" expected-response-code))
  (let [stringified-params (stringify-map params)
        response (services/app (compojure-request method resource stringified-params))]
    (if (= 302 (:status response))
      ;;(print-auth-failure-message)
      (println "302!!")
      )
    (condp = expected-response-code
      200 (request-successful? method response resource stringified-params)
      400 (request-unsuccessful? method response resource stringified-params)
      500 (request-error? method response resource stringified-params))
    (if *testing-trace*
      (println "Response status was:" (:status response) "*****\n"))
    response))

(defmacro web-requester [verb response-code]
  (let [method-prefix (str (name verb) "-request")
        method-name (condp = response-code
                      200 method-prefix
                      400 (str method-prefix "-unsuccessfully")
                      500 (str method-prefix "-with-error"))]
    `(defn ~(symbol method-name) [& [~'resource ~'params]]
       (web-request ~verb ~'resource ~'params ~response-code))))

(defmacro create-web-testers [& verbs]
  (let [success-deffers (map (fn [v] `(web-requester ~v 200)) verbs)
        unsuccess-deffers (map (fn [v] `(web-requester ~v 400)) verbs)
        failure-deffers (map (fn [v] `(web-requester ~v 500)) verbs)]
    `(do ~@success-deffers
         ~@unsuccess-deffers
         ~@failure-deffers)))

(create-web-testers :get :post :put :delete)