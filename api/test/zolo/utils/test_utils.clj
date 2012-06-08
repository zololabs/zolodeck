(ns zolo.utils.test-utils
  (:use [zolodeck.utils.debug])
  (:require [zolodeck.clj-social-lab.facebook.api :as api]
            [zolo.setup.config :as conf]))

(def HOBBES-FB-ID "100003928558336")

(defn hobbes-access-token []
  (let [app-access-token (api/app-access-token (conf/app-id) (conf/app-secret))
        all (api/all (conf/app-id) app-access-token)]
    (:access-token (first (filter #(= HOBBES-FB-ID (:id %)) all)))))