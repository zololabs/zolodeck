(ns zolo.service.distiller.social-identity
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.utils.logger :as logger]))

(defn distill [si]
  (select-keys si [:social/provider :social/provider-uid :social/ui-provider-uid]))