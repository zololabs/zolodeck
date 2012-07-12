(ns zolo.gigya.core
  (:use zolodeck.utils.debug)
  (:require [zolo.setup.config :as zolo-config])
  (:import com.gigya.socialize.SigUtils))

(defn validate-uid [uid signature-timestamp uid-signature]
  (SigUtils/validateUserSignature uid signature-timestamp (zolo-config/gigya-secret) uid-signature))


