(ns zolo.personas.generator
  (:use zolo.utils.debug
        zolo.utils.clojure
        zolo.demonic.test
        conjure.core)
  (:require [clojure.math.combinatorics :as combo]
            [zolo.marconi.facebook.core :as fb-lab]
            [zolo.personas.factory :as personas]
            [zolo.marconi.core :as marconi]
            [zolo.social.facebook.gateway :as fb-gateway]
            [zolo.social.facebook.messages :as fb-messages]
            [zolo.social.facebook.stream :as fb-stream]
            [zolo.social.core :as social]
            [zolo.domain.user :as user]
            [zolo.utils.maps :as zmaps]
            [zolo.store.user-store :as u-store]
            [zolo.service.user-service :as u-service]
            [zolo.domain.message :as message]))

(defn- no-of-msgs-per-interaction [no-of-interactions no-of-msgs]
  (let [outlier (mod no-of-msgs no-of-interactions)
        no-of-msgs-per-interaction (int (/ no-of-msgs no-of-interactions))]
    (map (fn [interaction-id] (if (= 1 interaction-id)
                               [interaction-id (+ no-of-msgs-per-interaction outlier)]
                               [interaction-id no-of-msgs-per-interaction]))
         (range 1 (+ no-of-interactions 1)))))

(defn- dummy-message [u friend thread-id msg-id]
  (let [user-friend (if (even? msg-id)
              [u friend]
              [friend u])]
    (concat user-friend
            [thread-id (str "Message ... User : " (:first_name friend) " - Thread-id :" thread-id " - Msg-id :" msg-id) (str "2012-05-" (+ 9 thread-id))])))

(defn- dummy-messages [u friend thread-id no-of-msgs]
  (map #(dummy-message u friend thread-id %)
       (range 1 (+ no-of-msgs 1))))

(defn- generate-messages [u friend no-of-i no-of-m]
  (let [no-of-msgs-per-interaction (no-of-msgs-per-interaction no-of-i no-of-m)]
    (mapcat (fn [[thread-id no-of-msgs]]
              (dummy-messages u friend thread-id no-of-msgs))
            no-of-msgs-per-interaction)))

(defn- generate-messages-for-friend [u friend no-of-i no-of-m]
  (doseq [msg-info (generate-messages u friend no-of-i no-of-m)]
    (apply fb-lab/send-message msg-info)))

(def DEFAULT-SPECS {:first-name "first"
                    :last-name "last"
                    :friends []})

(defn create-friend-spec
  ([f-name l-name no-of-i no-of-m]
     {:first-name f-name
      :last-name l-name
      :no-of-messages no-of-m
      :no-of-interactions no-of-i})
  ([f-name l-name]
     (create-friend-spec f-name l-name 0 0)))

(defn create-friend-specs [n]
  (map #(create-friend-spec (str "f-first-" %) (str "f-last-" %)) (range 0 n)))

(defn setup-facebook-ui [specs]
  (let [specs (merge DEFAULT-SPECS specs)
        u (fb-lab/create-user (:first-name specs) (:last-name specs))
        fs (map  (fn [f-spec]
                   [f-spec (fb-lab/create-user (:first-name f-spec) (:last-name f-spec))])
                 (:friends specs))]
    (fb-lab/login-as u)
    (doseq [[f-spec friend] fs]
      (fb-lab/make-friend u friend))
    (doseq [[f-spec friend] fs]
      (let [no-of-i (or (:no-of-interactions f-spec) 0)
            no-of-m (or (:no-of-messages f-spec) 0)]
        (cond
         (and (> no-of-m 0) (= 0 no-of-i)) (generate-messages-for-friend u friend 1 no-of-m)
         (< no-of-m no-of-i) (throw (RuntimeException. (str "Error Generating Persona :\n No of Messages :" no-of-m
                                                            " is less than No of Interactions:" no-of-i)))
         (> no-of-m 0) (generate-messages-for-friend u friend no-of-i no-of-m))))
    u))

(defn refresh-everything [db-u]
  (print-vals "REFRESHING:" db-u)
  (-> db-u
      u-service/refresh-user-data
      u-service/refresh-user-scores
      (print-vals-> "Refreshed everything:")))

(defn signup-with-facebook-ui [specs]
  (print-vals "Signup FB: " specs)
  (let [u (setup-facebook-ui specs)
        db-u (personas/create-db-user u)]
    (refresh-everything db-u)))

;; TODO - this needs to use u-service to add additional user-identities
(defn add-additional-facebook-ui [db-u specs]
  (let [fb-u (setup-facebook-ui specs)
        fb-ui (personas/fetch-fb-ui fb-u)]
    (-> db-u
        (update-in [:user/user-identities] conj fb-ui)
        u-store/save
        refresh-everything
        )))

(defn throw-unknown-ui-type [ui-type]
  (throw (RuntimeException. (str "Unknown UI-TYPE" ui-type))))

(defn signup-with-first-ui [{ui-type :UI-TYPE specs :SPECS}]
  (condp = ui-type
    :FACEBOOK (signup-with-facebook-ui specs)
    :else (throw-unknown-ui-type ui-type)))

(defn add-other-uis [u spec-combos]
  (reduce (fn [updating-u {ui-type :UI-TYPE specs :SPECS}]
            (condp = ui-type
              :FACEBOOK (add-additional-facebook-ui updating-u specs)
              :else (throw-unknown-ui-type ui-type)))
          u spec-combos))

(defn generate-user [spec-combo]
  (personas/in-social-lab
   (let [u (signup-with-first-ui (first spec-combo))
         u (add-other-uis u (rest spec-combo))]
     u)))

(defn get-spec-combos [specs]
  (let [ui-combos (combo/selections (:UI-IDS-ALLOWED specs) (:UI-IDS-COUNT specs))
        f-count (count (get-in specs [:SPECS :friends]))
        f-combos (filter #(= f-count (apply + %)) (combo/selections (range (inc f-count)) (:UI-IDS-COUNT specs)))
        ui-repeated (apply concat (repeat ui-combos))
        spec-pairs (map list ui-repeated f-combos)
        spec-combos (map (fn [[ui-combos f-combos]]
                           (map vector ui-combos f-combos)) spec-pairs)]
    spec-combos))

(defn partition-spec
  ([combo f-specs]
     (if-not (= (count f-specs) (apply + (map second combo)))
       (throw (RuntimeException. (str "Combo check failed for combo:" combo "f-specs:" f-specs))))
     (partition-spec combo f-specs []))
  ([combo f-specs results]
     (if (empty? combo)
       results
       (let [[ui-type f-count] (first combo)]
         (recur (rest combo)
                (drop f-count f-specs)
                (conj results {:UI-TYPE ui-type :SPECS {:friends (take f-count f-specs)}}))))))

(defn get-partitioned-specs [combos f-specs]
  (map #(partition-spec % f-specs) combos))

(defn generate [specs]
  (print-vals "SPEC-COMBOS:" (get-spec-combos specs))
  (generate-facebook (dissoc specs :UI-IDS-ALLOWED :UI-IDS-COUNT)))

(defn generate-domain [specs]
  (personas/domain-persona (generate specs)))