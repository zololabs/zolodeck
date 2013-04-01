(ns zolo.utils.test-utils
  (:use [zolo.utils.debug])
  (:require [zolo.marconi.facebook.api :as api]
            [zolo.setup.config :as conf]))

(def HOBBES-FB-ID "100003928558336")

(defn hobbes-access-token []
  (let [app-access-token (api/app-access-token (conf/fb-app-id) (conf/fb-app-secret))
        all (api/all (conf/fb-app-id) app-access-token)]
    (:access-token (first (filter #(= HOBBES-FB-ID (:id %)) all)))))