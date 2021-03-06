(ns zolo.api.contact-api
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.utils.http-status-codes
        zolo.api.core)
  (:require [zolo.service.contact-service :as c-service]
            [zolo.domain.contact :as contact]
            [zolo.utils.logger :as logger]
            [zolo.store.user-store :as u-store]
            [zolo.store.contact-store :as c-store]))

(defn find-contact [u-guid c-guid]
  (if-let [distilled-c (c-service/get-contact-by-guid (u-store/find-entity-by-guid u-guid) c-guid)]
    {:status (STATUS-CODES :ok)
     :body distilled-c}
    (resource-not-found "Contact")))

(defn update-contact [u-guid c-guid request-params]
  (if-let [distilled-c (c-service/update-contact (u-store/find-by-guid u-guid)
                                                 (c-store/find-by-guid c-guid)
                                                 request-params)]
    {:status (STATUS-CODES :created)
     :body distilled-c}
    (resource-not-found "Contact")))

(defn list-contacts [u-guid params]
  (if-let [contacts (c-service/list-contacts (u-store/find-entity-by-guid u-guid) params)]
    {:status (STATUS-CODES :ok)
     :body contacts}
    (resource-not-found "Contact")))




