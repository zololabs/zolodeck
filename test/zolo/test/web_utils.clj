(ns zolo.test.web-utils
  (:use zolo.utils.maps
        [clojure.test :only [run-tests deftest is are testing]]
        zolo.utils.debug
        conjure.core)
  (:require [zolo.core :as server]
            [clojure.data.json :as json]
            [zolo.web.fb-auth :as fb-auth]
            [zolo.domain.user-identity :as ui]))

(def DUMMY-FB-AUTH "RkFDRUJPT0sgVDRKNXY4aVM5bXhHTWltRFlkcjFfQ1ByR1hidUZoT1RvSWhOSkdFTXJlcy5leUpoYkdkdmNtbDBhRzBpT2lKSVRVRkRMVk5JUVRJMU5pSXNJbU52WkdVaU9pSkJVVUpqZDI1eVdWUXlNMmhzWWtvNGNEaFhZak5qY0RJNVJ6bGxlVTF5VG1Zd1FYUk1iVkF0Y2s0elRtSkROSGx4Wkc1VVRtZHNOR1JsTUZGWE1qaEJYMlpmU2xKdFZYSlZha054TTA5dFNXcFRkRzFvTjJSV1RqQjBWME14T1dWaVdURXRRazh0TjBaeldGWnFkSFV0UkUxb2VGbE5Na2t4VmxGUllqVk5lVE5zVlhBMVZGcHJaRFYyT0ZaS2JUTjZaV2d3WkVaNmJWWnBRMjlOTm13NUxVZDRWM1ZXUW5aVVp6RnBNRGxmTUVKWE56aGhUMWRXV2xOYWNteDRjelpMYmpORmRDMUxNM2x3VlVkZmFGTXRkamhpVjJ4bVgzTWlMQ0pwYzNOMVpXUmZZWFFpT2pFek5qWXlNek01TXpJc0luVnpaWEpmYVdRaU9pSXhNREF3TURNNU1qZzFOVGd6TXpZaWZR")

(defmacro with-mocked-auth [authenticated-user & body]
  `(stubbing [fb-auth/decode-signed-request {:user_id (ui/fb-id ~authenticated-user)}]
     (do ~@body)))

(defn compojure-request [method resource jsonified-body-str params]
  {:request-method method 
   :uri resource
   :params params
   :body (java.io.StringReader. jsonified-body-str)
   :headers {"accept" "application/vnd.zololabs.zolodeck.v1+json"
             "content-type" "application/json; charset=UTF-8"
             "authorization" DUMMY-FB-AUTH}
   :content-type "application/json; charset=UTF-8"})

(defn web-request
  ([method resource body]
     (web-request method resource body {}))
  ([method resource body params]
     (-> (compojure-request method resource (json/json-str body) params)
         server/app
         (update-in [:body] json/read-json))))

(defn authed-request
  ([user method resource body]
     (authed-request user method resource body {}))
  ([user method resource body params]
     (with-mocked-auth user
       (web-request method resource body params))))

;; (defn was-response-status? [{:keys [web-response] :as scenario} expected-status]
;;   (let [{:keys [status headers body]} web-response]
;;     (is (= expected-status status)))
;;   scenario)

;; (defn was-request-successful? [scenario]
;;   (was-response-status? scenario 200)
;;   scenario)

;; (defn new-user-url []
;;   "/users")