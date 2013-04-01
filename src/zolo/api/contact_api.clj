(ns zolo.api.contact-api
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.domain.user :as user]
            [zolo.domain.contact :as contact]
            [zolo.utils.logger :as logger]
            [zolo.utils.fe :as fe]))

;; (defn list-contacts [request-params]
;;   (map fe/format-contact-info
;;        (-> (user/current-user)
;;            (contact/list-contacts {}))))

;; (defn update-contact [request-params]
;;   (-> (:guid request-params)
;;       contact/find-by-guid-string
;;       (contact/set-muted (:muted request-params))
;;       contact/reload
;;       :contact/muted))


