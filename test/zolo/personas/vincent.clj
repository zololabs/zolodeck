(ns zolo.personas.vincent
  (:use zolo.utils.debug
        conjure.core
        zolo.demonic.core)
  (:require [zolo.marconi.facebook.core :as fb-lab]
            [zolo.domain.contact :as contact]
            [zolo.personas.factory :as personas]
            [zolo.personas.generator :as pgen]
            [zolo.domain.user :as user]
            [zolo.service.user-service :as u-service]))

(defn create []
  (pgen/generate {:SPECS {:first-name "Vincent"
                          :last-name "Fong"
                          :friends [(pgen/create-friend-spec "Jack" "Daniels" 2 3)
                                    (pgen/create-friend-spec "Jill" "Ferry" 1 2)]}}))

(defn create-domain []
  (personas/domain-persona (create)))