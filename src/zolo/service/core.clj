(ns zolo.service.core
  (:use zolo.utils.debug
        [slingshot.slingshot :only [throw+]])
  (:require [zolo.utils.domain.validations :as validations]
            [zolo.domain.user :as user]
            [zolo.social.core :as social]))

(defn validate-request! [request-params validation-map]
  (let [[valid? messages] (validations/valid? validation-map request-params)]  
    (if valid?
      request-params
      (throw+ {:type :bad-request :error messages}))))

(defn provider-string->provider-enum [provider-string]
  (or (social/provider-enum (.toUpperCase provider-string))
      (throw (RuntimeException. (str "Unknown provider string specified: " provider-string)))))

(defn missing-message-error [ui-guid m-id]
  (str "Message not found for UI-GUID, MESSAGE-ID: [" ui-guid "," m-id "]"))

(defn missing-user-error [user-guid]
  (str "User not found for USER-GUID: [" user-guid "]"))

(defn missing-contact-error [c-guid]
  (str "User not found for CONTACT-GUID: [" c-guid "]"))

(defmacro let-user-entity [[user-sym user-guid] & body]
  `(zolo.utils.clojure/unless-log [~user-sym (zolo.store.user-store/find-entity-by-guid ~user-guid)] (zolo.service.core/missing-user-error ~user-guid)
                                  ~@body))

(defmacro let-user [[user-sym user-guid] & body]
  `(zolo.utils.clojure/unless-log [~user-sym (zolo.store.user-store/find-by-guid ~user-guid)] (zolo.service.core/missing-user-error ~user-guid)
                                  ~@body))

(defmacro let-message-entity [[message-sym ui-guid message-id] & body]
  `(zolo.utils.clojure/unless-log
    [~message-sym (zolo.store.message-store/find-by-ui-guid-and-id ~ui-guid ~message-id)] (zolo.service.core/missing-message-error ~ui-guid ~message-id)
    ~@body))

(defmacro let-contact [[contact-sym c-guid] & body]
  `(zolo.utils.clojure/unless-log
    [~contact-sym (zolo.store.contact-store/find-by-guid ~c-guid)] (zolo.service.core/missing-contact-error ~c-guid)
    ~@body))

(defmacro let-contact-entity [[contact-sym c-guid] & body]
  `(zolo.utils.clojure/unless-log
    [~contact-sym (zolo.store.contact-store/find-entity-by-guid ~c-guid)] (zolo.service.core/missing-contact-error ~c-guid)
    ~@body))