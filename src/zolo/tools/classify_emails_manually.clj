(ns zolo.tools.classify-emails-manually
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.social.email.gateway :as email]
            [zolo.utils.calendar :as zcal]
            [zolo.utils.string :as zstring]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [zolo.gateway.pento.core :as pento]))

(defn all-contacts-details- [cio-account-id]
  (email/get-contacts cio-account-id (zcal/to-seconds #inst "2000-01-01")))

(def all-contacts-details (memoize all-contacts-details-))

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
          (.split it "\n")
          (map json/read-json it)))

(defn progress [file-name]
  (try
    (->> file-name
         read-file
         (group-by :email))
    (catch Exception e
      {})))

(defn dump-file [data file]
  (it-> data
       (map json/json-str it)
       (str/join "\n" it)
       (str it "\r\n")
       (spit file it)))

(defn swap-last [from-file to-file]
  (let [from-data (read-file from-file)
        to-data (read-file to-file)]
    (dump-file (butlast from-data) from-file)
    (dump-file (concat to-data (list (last from-data))) to-file))
  (print-vals "Undo complete!"))

(defn print-contact-info [cd score]
  (print-vals "Contact:" (:name cd) "|" (:email cd) "| Pento:" score))

(defn write-person [cd file]
 (spit file (contact-json cd) :append true))

(defn classify [contact-details person-file not-person-file]
  (print-vals "Starting...")
  (print-vals "Will process" (count contact-details) "contacts...")
  (let [person-progress (progress person-file)
        not-person-progress (progress not-person-file)]
    (loop [cd (first contact-details)
           remaining (rest contact-details)]
      (print-vals "Remaining count:" (count remaining))
      (if-not (processed? cd person-progress not-person-progress)
        (let [score (pento/score-all [cd])]
          (do
            (print-contact-info cd score)
            (println "Person? [y]/n/uy/un/x:")
            (let [choice (user-choice)]
              (print-vals "OK," choice)
              (condp = choice
                "" (write-person cd person-file)
                "Y" (write-person cd person-file)
                "N" (write-person cd not-person-file)
                "UY" (swap-last person-file not-person-file)
                "UN" (swap-last not-person-file person-file)
                (print-vals "Quiting."))
              (condp = choice
                "UY" (recur cd remaining)
                "UN" (recur cd remaining)
                "Y" (recur (first remaining) (rest remaining))
                "N" (recur (first remaining) (rest remaining))
                "" (recur (first remaining) (rest remaining))
                (print-vals "Done.")))))
        (recur (first remaining) (rest remaining))))))

(defn go! [cio-account-id person-output-file not-person-output-file]
  (print-vals "Fetching contacts...")
  (-> cio-account-id
      all-contacts-details
      (classify person-output-file not-person-output-file)))