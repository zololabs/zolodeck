(ns zolo.tools.classify-emails-manually
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.social.email.gateway :as email]
            [zolo.utils.calendar :as zcal]
            [clojure.data.json :as json]
            [clojure.string :as str]))

(defn all-contacts-details [cio-account-id]
  (email/get-contacts cio-account-id (zcal/to-seconds #inst "2000-01-01")))

(defn contact-json [contact-details]
  (-> contact-details
      (select-keys [:email :name :received_count :sent_count])
      json/json-str
      (str "\r\n")))

(defn user-choice []
  (try
    (let [input (.toUpperCase (read-line))]
      (if (some #{input} ["Y" "N" "X" "" "UY" "UN"])
        input
        (user-choice)))
    (catch Exception e
      (user-choice))))

(defn processed? [contact previous-goods previous-bads]
  (or (previous-goods (:email contact))
      (previous-bads  (:email contact))))

(defn read-file [file-name]
  (it-> file-name
          (slurp it)
          (.split it "\r\n")
          (map json/read-json it)))

(defn progress [file-name]
  (try
    (->> file-name
         read-file
         (group-by :email))
    (catch Exception e
      {})))

(defn dump-file [data file]
  (->> data
       (str/join "\r\n")
       (spit file)))

(defn swap-last [from-file to-file]
  (let [from-data (read-file from-file)
        to-data (read-file to-file)]
;    (spit from-file (butlast from-data))
;    (spit to-file (conj to-data (last from-data)))
    (dump-file (butlast from-data) from-file)
    (dump-file (conj to-data (last from-data)) to-file)))

(defn classify [contact-details person-file not-person-file]
  (print-vals "Starting...")
  (print-vals "Will process" (count contact-details) "contacts...")
  (let [person-progress (progress person-file)
        not-person-progress (progress not-person-file)]
    (loop [cd (first contact-details)
           remaining (rest contact-details)]
      (print-vals "Remaining count:" (count remaining))
      (if-not (processed? cd person-progress not-person-progress)
        (do
          (print-vals "Contact:" (:name cd) "|" (:email cd))
          (println "Person? [y]/n/uy/un/x:")
          (let [choice (user-choice)]
            (print-vals "OK," choice)
            (condp = choice
              "" (spit person-file (contact-json cd) :append true)
              "Y" (spit person-file (contact-json cd) :append true)
              "N" (spit not-person-file (contact-json cd) :append true)
              "UY" (swap-last person-file not-person-file)
              "UN" (swap-last person-file not-person-file)
              (print-vals "Quiting."))
            (if-not (= "X" choice)
              (recur (first remaining) (rest remaining)))))
        (recur (first remaining) (rest remaining))))))

(defn go! [cio-account-id person-output-file not-person-output-file]
  (print-vals "Fetching contacts...")
  (-> cio-account-id
      all-contacts-details
      (classify person-output-file not-person-output-file)))