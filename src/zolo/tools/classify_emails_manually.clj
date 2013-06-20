(ns zolo.tools.classify-emails-manually
  (:use zolo.utils.debug
        zolo.utils.clojure)
  (:require [zolo.social.email.gateway :as email]
            [zolo.utils.calendar :as zcal]
            [clojure.data.json :as json]))

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
      (if (or (= "Y" input) (= "N" input) (= "X" input))
        input
        (user-choice)))
    (catch Exception e
      (user-choice))))

(defn processed? [contact previous-goods previous-bads]
  (or (previous-goods (:email contact))
      (previous-bads  (:email contact))))

(defn progress [file-name]
  (it-> file-name
        (slurp it)
        (.split it "\r\n")
        (map json/read-json it)
        (group-by :email it)))

(defn classify [contact-details person-file not-person-file]
  (let [person-progress (progress person-file)
        not-person-progress (progress not-person-file)]
    (loop [cd (first contact-details)
           remaining (rest contact-details)]
      (if-not (processed? cd person-progress not-person-progress)
        (do
          (print-vals "Contact:" (:name cd) "|" (:email cd))
          (println "Person? [y/n/x]")
          (let [choice (user-choice)]
            (condp = choice
              "Y" (spit person-file (contact-json cd) :append true)
              "N" (spit not-person-file (contact-json cd) :append true)
              (print-vals "Quiting."))
            (if-not (= "X" choice)
              (recur (first remaining) (rest remaining)))))
        (recur (first remaining) (rest remaining))))))

(defn go! [cio-account-id person-output-file not-person-output-file]
  (print-vals "Fetching contacts...")
  (-> cio-account-id
      all-contacts-details
      (classify person-output-file not-person-output-file)))