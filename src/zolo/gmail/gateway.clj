(ns zolo.incoming.gateway
   (:use clojure.contrib.java-utils
         zolodeck.utils.calendar)
   (:import (javax.mail Session Folder Flags)
            (javax.mail.search FlagTerm ComparisonTerm ReceivedDateTerm)
            (javax.mail Flags$Flag)))

(def GMAIL-ALL-MAIL "[Gmail]/All Mail")

 (defn open-store [protocol server user pass]
   (let [p (as-properties [["mail.store.protocol" protocol]])]
     (doto (.getStore (Session/getDefaultInstance p) protocol)
       (.connect server user pass))))

 (defn open-gmail-store [username password]
   (store  "imaps" "imap.gmail.com" username password))

(defn folder [store folder-name]
  (doto (.getFolder store folder-name)
    (.open Folder/READ_ONLY)))

(defn gmail-all-mail-folder [username password]
  (folder (open-gmail-store username password) GMAIL-ALL-MAIL))

(defn fetch-mail [from-date username password]
  (let [gmail-folder (gmail-all-mail-folder username password)
        newer-than (ReceivedDateTerm. ComparisonTerm/GT (date-string->instant :date from-date))]
    (.search gmail-folder newer-than)))