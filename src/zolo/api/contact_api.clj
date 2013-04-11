(ns zolo.api.contact-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.web.status-codes
        zolo.api.core)
  (:require [zolo.service.contact-service :as c-service]
            [zolo.domain.contact :as contact]
            [zolo.utils.logger :as logger]
            [zolo.store.user-store :as u-store]))

;; GET /users/guid
(defn find-contact [u-guid c-guid]
  (if-let [distilled-c (c-service/get-contact-by-guid (u-store/find-by-guid u-guid) c-guid)]
    {:status (STATUS-CODES :ok)
     :body distilled-c}
    (resource-not-found)))

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




