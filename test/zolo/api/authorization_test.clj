(ns zolo.api.authorization-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        zolo.demonic.test
        zolo.demonic.core
        zolo.utils.debug
        zolo.utils.clojure
        zolo.test.core-utils
        conjure.core
        zolo.web.status-codes)
  (:require [zolo.domain.user :as user]
            [zolo.test.web-utils :as w-utils]
            [zolo.api.user-api :as user-api]
            [zolo.domain.user-identity :as ui]
            [zolo.personas.factory :as personas]
            [zolo.marconi.core :as marconi]
            [zolo.marconi.facebook.core :as fb-lab]
            [clojure.data.json :as json]
            [zolo.service.user-service :as u-service]
            [zolo.core :as server]
            [zolo.personas.generator :as pgen]
            [zolo.domain.core :as d-core]
            [zolo.domain.contact :as contact]
            [zolo.social.facebook.chat :as fb-chat]
            [zolo.test.assertions.datomic :as db-assert]))

(defn contacts-url [u c]
  (str "/users/" (or (:user/guid u) (random-guid-str))
       "/contacts/" (or (:contact/guid c) (random-guid-str))))

(defn ss-url [u]
  (str "/users/" (or (:user/guid u) (random-guid-str)) "/suggestion_sets"))

(defn c-stats-url [u]
  (str "/users/" (or (:user/guid u) (random-guid-str)) "/contact_stats"))

(defn i-stats-url [u]
  (str "/users/" (or (:user/guid u) (random-guid-str)) "/interaction_stats"))

(defn messages-url [u]
  (str "/users/" (or (:user/guid u) (random-guid-str)) "/messages"))

(defn user-url [u]
  (str "/users/" (or (:user/guid u) (random-guid-str))))

(demonictest test-users-context
  (stubbing [fb-chat/send-message true]
    (run-as-of "2012-12-21"
      (d-core/run-in-gmt-tz
       (personas/in-social-lab
        (let [owner (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                                      (pgen/create-friend-spec "Jill" "Ferry" 1 1)]}})
              [jack jill] (sort-by contact/first-name (:user/contacts owner))
              hacker (pgen/generate {:SPECS {:friends []}})]
          
          (are [expected user method url params] (= expected (-> (w-utils/authed-request user method url params) :status))

               ;;Users
               404 hacker :put  (user-url owner)             {:login_provider "FACEBOOK" :permissions_granted false :login_tz 420}
               200 owner  :put  (user-url owner)             {:login_provider "FACEBOOK" :permissions_granted false :login_tz 420}

               404 hacker :get  (user-url owner)             {}
               200 owner  :get  (user-url owner)             {}

               ;;Suggestion Sets
               404 hacker :get  (ss-url owner)               {}
               200 owner  :get  (ss-url owner)               {}

               ;;Contacts
               404 hacker :get  (contacts-url owner jack)    {}
               200 owner  :get  (contacts-url owner jack)    {}

               ;; Messages
               404 hacker :post (messages-url owner)    {:text "Hey" :provider "facebook" :to ["123"] :guid (-> owner :user/guid str)}
               201 owner  :post (messages-url owner)    {:text "Hey" :provider "facebook" :to ["123"] :guid (-> owner :user/guid str)}

               ;;Stats
               404 hacker :get  (c-stats-url owner)           {}
               200 owner  :get  (c-stats-url owner)           {}

               404 hacker :get  (i-stats-url owner)           {}
               200 owner  :get  (i-stats-url owner)           {}
               )))))))


(demonictest test-find-users
  (let [owner (pgen/generate {:SPECS {:friends [(pgen/create-friend-spec "Jack" "Daniels" 1 1)
                                                (pgen/create-friend-spec "Jill" "Ferry" 1 1)]}})
        hacker (pgen/generate {:SPECS {:friends []}})
        owner-params {:login_provider "FACEBOOK" :login_provider_uid (ui/fb-id owner)}]

    (testing "when owner is requesting with login provider uid it should return resource"
      (is (= 200 (-> (w-utils/authed-request owner :get "/users" owner-params) :status))))

    (testing "when hacker is requesting with login provider uid of owner it should deny permission"
      (is (= 404 (-> (w-utils/authed-request hacker :get "/users" owner-params) :status))))))

