(ns zolo.personas.shy
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
  (pgen/generate {:SPECS {:first-name "Shy"
                          :last-name "Hallow"
                          :friends [
                                       (pgen/create-friend-spec "Jack" "Daniels")
                                       (pgen/create-friend-spec "Jill" "Ferry")]}}))

(defn create-domain []
  (personas/domain-persona create))