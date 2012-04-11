(ns zolo.incoming.facebook.inbox
  (:use [clojure.core.match :only [match]]
        zolo.incoming.facebook.gateway))


(defn fetch-inbox-threads
  ([auth-token start-date]
     (run-fql auth-token "SELECT thread_id, updated_time, subject, recipients 
                          FROM thread 
                          WHERE folder_id = 0 "))
  ([auth-token]
     (fetch-inbox-threads )))