(ns zolo.personas.vincent
  (:use zolo.utils.debug
        conjure.core
        zolo.demonic.core)
  (:require [zolo.marconi.facebook.core :as fb-lab]
            [zolo.domain.contact :as contact]
            [zolo.personas.factory :as personas]
            [zolo.personas.generator :as p-generator]
            [zolo.domain.user :as user]
            [zolo.service.user-service :as u-service]))

(defn create []
  (p-generator/generate {:first-name "Vincent"
                         :last-name "Fong"
                         :friends [
                                   {:first-name "Jack"
                                    :last-name "Daniels"
                                    :no-of-messages 3
                                    :no-of-interactions 2}
                                   {:first-name "Jill"
                                    :last-name "Ferry"
                                    :no-of-messages 2
                                    :no-of-interactions 1}]}))

(defn create-domain []
  (personas/domain-persona create))