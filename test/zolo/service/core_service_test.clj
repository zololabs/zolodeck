(ns zolo.service.core-service-test
  (:use zolo.utils.debug
        clojure.test
        zolo.demonic.test)
  (:require [zolo.personas.factory :as personas]
            [zolo.test.assertions.datomic :as db-assert]
            [zolo.test.assertions.domain :as d-assert]
            [zolo.service.contact-service :as c-service]
            [zolo.service.message-service :as m-service]
            [zolo.domain.message :as message]
            [zolo.store.user-store :as u-store]
            [zolo.store.message-store :as m-store]
            [zolo.marconi.facebook.core :as fb-lab]))

