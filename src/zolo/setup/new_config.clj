(ns zolo.setup.new-config
  (:use zolodeck.utils.clojure
        zolodeck.utils.debug))

(def ^:dynamic ENV "staging")

(def CONFIG-MAP
  {
   :datomic-db "datomic:free://staging.zolodeck.com:4334/zolodeck-staging"
   
   :kiss-api "0574cc154095cc7ddcaa04480daa22903da7f1b7"
   
   :context-io-key "lq4k7hlv"
   :context-io-secret "8KDEzAPKKn0whYT9"
   
   :fb-app-id "361942873847116"
   :fb-app-secret "6a968bdeb2eb92ac913ec1b88f88cef6"
   
   :li-api-key "p8rw9l9pzvl8"
   :li-secret-key "1thkgbvKwrcFdZ7N"

   :user-update-wait-fb-millis (* 1000 60) ;; 1 minutes
   :stale-users-wait-fb-millis (* 1000 60) ;; 1 minute
   })

(defn production-mode? []
  (= :production ENV))

(defn environment []
  ENV)

(defn datomic-db-name [] 
  (get-in CONFIG-MAP [:datomic-db]))

(defn fb-app-id []
  (get-in CONFIG-MAP [:fb-app-id]))

(defn fb-app-secret []
  (get-in CONFIG-MAP [:fb-app-secret]))

(defn user-update-wait-fb-millis []
  (get-in CONFIG-MAP [:user-update-wait-fb-millis]))

(defn stale-users-wait-fb-millis []
  (get-in CONFIG-MAP [:stale-users-wait-fb-millis]))

(defn li-api-key []
  (get-in CONFIG-MAP [:li-api-key]))

(defn li-secret-key []
  (get-in CONFIG-MAP [:li-secret-key]))

(def FB-AUTH-COOKIE-NAME (str "fbsr_" (fb-app-id)))

(def LI-AUTH-COOKIE-NAME (str "linkedin_oauth_" (li-api-key)))

(def GIT-HEAD-SHA "GIT-SHA-NOT-SET")