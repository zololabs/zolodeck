(ns zolo.domain.user-identity
  (:use zolodeck.utils.debug))

(defn is-provider? [provider ui]
  (= provider (:identity/provider ui)))

(defn is-fb? [ui]
  (is-provider? :provider/facebook ui))

