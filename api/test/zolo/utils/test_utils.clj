(ns zolo.utils.test-utils
  (:require [zolodeck.clj-social-lab.facebook.api :as api]))

(defn hobbes-access-token []
  (let [all (api/all api/APP-ID api/APP-ACCESS-TOKEN)]
    (:access-token (first (filter #(= "100003858741258" (:id %)) all)))))