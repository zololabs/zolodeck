(ns zolo.utils.gigya)

(def FACEBOOK "facebook")

(defn is-facebook-login? [gigya-user]
  (= FACEBOOK (:loginProvider gigya-user)))

(defn facebook-identity [gigya-user]
  (get-in gigya-user [:identities :facebook]))

(defn identities [gigya-user]
  (vals (:identities gigya-user)))

