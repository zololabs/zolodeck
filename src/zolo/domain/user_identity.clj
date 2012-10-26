(ns zolo.domain.user-identity
  (:use zolodeck.utils.debug))

(defn is-fb? [ui]
  (= :provider/facebook (:identity/provider ui)))