(ns zolo.social.facebook.core
  (:use zolodeck.utils.debug)
  (:require [zolo.social.core :as social]))

;; TODO add schema validation check for this API (facebook login)
(defmethod social/login-user social/FACEBOOK [request-params]
  (let [{access-token :accessToken user-id :userID signed-request :signedRequest} (get-in request-params [:providerLoginInfo :authResponse])]
    (print-vals "AT:" access-token)
    (print-vals "u-ID:" user-id)
    (print-vals "signed-request:" signed-request)
    "done"))