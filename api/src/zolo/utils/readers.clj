(ns zolo.utils.readers)

(defn guid [four-letters-four-digits]
  (java.util.UUID/fromString (str four-letters-four-digits "-1000-413f-8a7a-f11c6a9c4036")))