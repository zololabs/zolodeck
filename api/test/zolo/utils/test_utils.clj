(ns zolo.utils.test-utils
  (:require [zolodeck.clj-social-lab.facebook.api :as api]
            [zolo.setup.config :as conf]))

(defn hobbes-access-token []
  (let [app-access-token (api/app-access-token (conf/app-id) (conf/app-secret))
        all (api/all (conf/app-id) app-access-token)]
    (:access-token (first (filter #(= "100003858741258" (:id %)) all)))))