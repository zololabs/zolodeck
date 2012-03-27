(ns zolo.config)

(declare CONFIG-MAP ENV)

(defn load-config [config-file env]
  (def ENV env)
  (def CONFIG-MAP (load-string (slurp config-file))))

(defn production-mode? []
  (= :production ENV))

(defn db-spec []
  (get-in CONFIG-MAP [ENV :db-spec]))

(defn mail-spec []
  (get-in CONFIG-MAP [ENV :mail-spec]))

(defn apns-password []
  (get-in CONFIG-MAP [ENV :apns-password]))

(defn apns-env []
  (get-in CONFIG-MAP [ENV :apns-env]))

(defn apns-production? []
  (= :production (apns-env)))

(defn yipit-key []
  (get-in CONFIG-MAP [ENV :yipit-key]))

(defn yipit-limit []
  (get-in CONFIG-MAP [ENV :yipit-limit]))

(defn config-folder []
  (-> (.get (System/getenv) "INSTAFUN_HOME")
      (str "/config")))

(defn certificate-filename []
  (str (config-folder) "/certificates/mfj_" (name (apns-env))  "_push.p12"))

(defn setup-config []
  (let [config-file (str (config-folder) "/insta.conf")
        env (keyword (.get (System/getenv) "INSTAFUN_ENV"))]
    (load-config config-file env)))